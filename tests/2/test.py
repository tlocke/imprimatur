[
    {
        'name': "single regex success",
        'port': 5000,
        'host': "localhost",
        'path': "/text_2",
        'status_code': 200,
        'regexes': ['inexhaustible']},
    {
        'name': "status code as string",
        'path': "/text_1",
        'status_code': '200'},
    {
        'name': "Auth success",
        'path': "/auth",
        'auth': ("conrad", "kurtz"),
        'status_code': 200},
    {
        'name': "Auth carried over success",
        'path': "/auth",
        'status_code': 200},
    {
        'name': "Relative file upload success",
        'path': "/echo",
        'method': "post",
        'files': {"yellow": "tests/2/crome-file.txt"},
        'regexes': ["yellow", "on the leads"],
        'status_code': 200},
    {
        'name': "Post name and value with regex. Success.",
        'path': "/echo",
        'method': "post",
        'data': {"quote": "Leisure is the mother of philosophy."},
        'status_code': 200,
        'regexes': ["Leisure"]},
    {
        'name': "Reset tries. Success.",
        'path': "/counter_reset",
        'status_code': 200},
    {
        'name': "Tries. Success.",
        'path': "/counter",
        'tries': {'max': 4, 'period': 1},
        'status_code': 200,
        'regexes': ["The Decline And Fall Of The Roman Empire"]},
    {
        'name': "Regex on header. Success",
        'path': "/redirect?location=http://localhost:5000/here.html",
        'status_code': 302,
        'regexes': [
            "('[Ll]ocation', 'http://localhost:5000/here.html')",
            # . means all characters, even line breaks
            "[Cc]ontent-[Tt]ype.*[Ll]ocation"]},
    {
        'name': "HTTP HEAD request. Success",
        'path': "/text_1",
        'method': "head",
        'status_code': 200},
    {
        'name': "Upload script with syntax errors",
        'path': "/",
        'method': 'post',
        'files': {"file": "tests/2/syntax_error.py"},
        'status_code': 303},
    {
        'name': "Custom HTTP headers",
        'path': '/text_1',
        'headers': {'qubit': "deutsch"},
        'status_code': 200},
    {
        'name': "Binary file upload success",
        'path': "/echo_bin",
        'method': "post",
        'files': {"yellow": "tests/2/binary.bin"},
        'status_code': 200},
    {
        'name': "Unzip",
        'path': "/text_2_zip",
        'unzip': True,
        'status_code': 200,
        'regexes': ['inexhaustible']},
    {
        'name': "Default path",
        'status_code': 200}]
