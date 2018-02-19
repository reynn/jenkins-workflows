import jinja2
from pathlib import Path
import os
import re
import sys
import yaml

from argparse import ArgumentParser
from tabulate import tabulate

METHOD_DEF_REGEX = re.compile(r'^public (?P<method_name>.+?)\((?P<method_args>.+?)\) \{$')
METHOD_END_REGEX = re.compile(r'^\}$')
SCRIPT_PATH = Path()


class Arg:
	def __init__(self, arg_type, arg_name) -> None:
		self.type = arg_type
		self.name = arg_name

	def __repr__(self) -> str:
		return f"Arg({self.type}: {self.name})"

	@staticmethod
	def parse(data):
		args = []
		for arg in [x.strip() for x in data.split(',')]:
			arg_def = [a for a in arg.split(' ')]
			if len(arg_def) > 1:
				arg_type = arg_def[0]
				arg_name = arg_def[1]
			else:
				arg_type = 'Object'
				arg_name = arg_def[0]

			args.append(Arg(arg_type, arg_name))
		return args


class Method:
	def __init__(self, method_name, method_body, method_args, doc):
		self.name = method_name
		self.args = Arg.parse(method_args)
		self.body = method_body
		self.doc = doc

	def __repr__(self) -> str:
		return f"Method({self.method_name}, {self.method_args})"


def parse_file(file_lines):
	functions = []

	function_groups = {}

	function_start_line = 0
	function_end_line = 0

	for i, f in enumerate(file_lines):
		if f == '\n':
			continue
		line_is_method_def = METHOD_DEF_REGEX.match(f)
		if function_start_line != 0:
			if METHOD_END_REGEX.match(f):
				function_end_line = i
				function_doc_lines = []
				if file_lines[function_start_line-1].strip() == '*/':
					doc_end_line = function_start_line-1
					for doc_i, line in enumerate(file_lines[doc_end_line::-1]):
						if line == '/*':
							doc_start_line = doc_end_line - doc_i
							function_doc_lines = file_lines[doc_start_line+1:doc_end_line]
							break
				functions.append(
					Method(
						method_name=function_groups.get('method_name'),
						method_args=function_groups.get('method_args'),
						method_body=file_lines[function_start_line+1:function_end_line-1],
						doc='\n'.join([x for x in function_doc_lines if x != '\n'])
					)
				)
				function_start_line = 0
		else:
			if line_is_method_def:
				function_start_line = i
				function_groups = line_is_method_def.groupdict()

	return functions


def get_workflow_doc(file_lines):
	workflow_doc_start_line = 0
	workflow_doc_end_line = 0
	for i, f in enumerate(file_lines):
		if workflow_doc_start_line == 0:
			if f.startswith("workflowDoc = '''"):
				workflow_doc_start_line = i
		else:
			if f.strip() == "'''":
				workflow_doc_end_line = i
				break

	if workflow_doc_start_line != 0 and workflow_doc_end_line != 0:
		return [x for x in file_lines[workflow_doc_start_line+1:workflow_doc_end_line] if x != '']


def parse_yaml(text):
	return yaml.load(text)


def represent_none(self, _):
	return self.represent_scalar('tag:yaml.org,2002:null', '')


def to_yaml(contents):
	yaml.add_representer(type(None), represent_none)
	return yaml.dump(contents, default_flow_style=False)


def sort_columns(l):
	ret_l = []
	if 'name' in l:
		ret_l.append('name')
		l.remove('name')
	if 'required' in l:
		ret_l.append('required')
		l.remove('required')
	if 'type' in l:
		ret_l.append('type')
		l.remove('type')
	if 'description' in l:
		ret_l.append('description')
		l.remove('description')
	if len(l) > 0:
		for i in sorted(l):
			ret_l.insert(len(ret_l) - 1, i)
			l.remove(i)

	return ret_l


def create_markdown_table(values):
	rows = []
	columns = []
	for x in [v.keys() for v in values]:
		for c in x:
			if c not in columns:
				columns.append(c)

	columns = sort_columns(columns)

	for row in values:
		d = []
		for col in columns:
			if row.get(col) is None:
				d.append('')
			else:
				if col == 'default':
					d.append(f"`{row.get(col)}`")
				else:
					if col == 'required':
						d.append(
							f"{'Required' if row.get(col) == True else 'Optional'}")
					else:
						d.append(row.get(col))
		rows.append(d)

	return tabulate(rows, [x.title() for x in columns], tablefmt="pipe")


def render_jinja_template(tpl_path, context):
	path, filename = os.path.split((SCRIPT_PATH / tpl_path).resolve())
	print(f"Searching for templates in {path}")
	env = jinja2.Environment(
		loader=jinja2.FileSystemLoader(path)
	)
	print(f"Available templates {env.list_templates('j2')}")
	return env.get_template(filename).render(context)


def create_index_markdown(groovy_files, docs_folder):
	print('Generating index.md ...')
	links = []
	for groovy_file in sorted(groovy_files):
		workflow_name = os.path.splitext(groovy_file)[0]
		links.append(
			f"* [{workflow_name.title()}]({workflow_name.upper()}.md)")

	rendered_template = render_jinja_template('.github/PAGES_INDEX.md.j2', {'WORKFLOW_LINKS': '\n'.join(links)})

	with open(docs_folder / 'index.md', 'w') as w:
		w.write(rendered_template + '\n')


def create_markdown_doc(name, docs_folder, workflow_doc, functions):
	docs_folder.mkdir(exist_ok=True)
	lines = [f"# {name.replace('.groovy', '').title()}"]
	if workflow_doc:
		file_docs = parse_yaml(str('\n'.join(workflow_doc)))
		overview = file_docs.get('overview')
		if overview:
			lines.append(f"\n## Overview")
			lines.append(f"\n> {overview}")
		if file_docs.get('tools'):
			lines.append('\n## Tools Section')
			lines.append(f"\n{create_markdown_table(file_docs.get('tools'))}")
		lines.append('\n## Available Methods')
		for g_function in functions:
			if g_function.name in ['getStageName', 'tests']:
				continue
			function_name = g_function.name.strip('\'')
			lines.append(f"\n### {function_name}")
			if g_function.doc:
				function_yaml_def = parse_yaml(g_function.doc)
				lines.append(f"\n> {function_yaml_def.get('description')}")
				lines.append(f"\n{create_markdown_table(function_yaml_def.get('parameters'))}")
				lines.append(f"\n### {function_name} Example")
				lines.append(f"\n```yaml\n{to_yaml(function_yaml_def.get('example'))}```")

		if file_docs.get('full_example'):
			lines.append('\n## Full Example Pipeline')
			lines.append(f"\n```yaml\n{file_docs.get('full_example')}\n```")

		if file_docs.get('additional_resources'):
			lines.append('\n## Additional Resources\n')
			for resource in file_docs.get('additional_resources'):
				lines.append(f"* [{resource.get('name')}]({resource.get('url')})")
		with (docs_folder / name.upper().replace('.GROOVY', '.md')).open(mode='w') as w:
			w.write('\n'.join(lines))
	else:
		print(f"{name} does not contain a workflow_doc = '''''' string. exiting.")
		exit(2)


def entry_point():
	parser = ArgumentParser()
	parser.add_argument('-o', '--out-path')

	args = parser.parse_args()

	if args.out_path is None:
		parser.print_usage()
		exit(1)

	for fil in SCRIPT_PATH.glob('*.groovy'):
		if fil.name.startswith('example'):
			continue
		with fil.open() as groovy_file:
			print(f"Generating documentation for {fil.name}...")
			lines = groovy_file.read().splitlines()
			workflow_doc = get_workflow_doc(lines)
			functions = parse_file(lines)
			create_markdown_doc(name=fil.name,
								docs_folder=SCRIPT_PATH / args.out_path,
								workflow_doc=workflow_doc,
								functions=functions)
	create_index_markdown([x.name for x in SCRIPT_PATH.glob('*.groovy') if x.name != 'example.groovy'], SCRIPT_PATH / args.out_path)


if __name__ == '__main__':
	entry_point()
