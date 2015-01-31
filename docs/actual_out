[

    # Initial comment

    # Request test_01
    {
        'name': "single regex success",
        'port': '8080',
        'host': 'localhost',
        'path': '/imprimatur/test_01.html',

        # Check status code
        'status_code': 200,
        'regexes': [
            r"inexhaustible", ], },

    # Request test_01
    {
        'path': '/imprimatur/test_01.html',

        # Check status code
        'status_code': 200,
        'regexes': [
            r"inexhaustible", ], },

    # Check credentials
    {
        'name': "credentials success",
        'path': '/imprimatur/test_01_credentials.html',
        'auth': ('conrad', 'kurtz'),
        'tries': {'number': 10, 'period': 1},

        # Wait in millis
        'status_code': 200, },
    {
        'name': "relative file upload success",
        'path': '/imprimatur/echo',
        'method': 'post',
        'files': {'yellow': 'crome-file.txt'},
        'tries': {'number': 10, 'period': 1},
        'regexes': [
            r"yellow",
            r"on the leads", ],
        'status_code': 200, },
    {
        'name': "Post with control value as 'value' attribute.",
        'path': '/imprimatur/echo',
        'method': 'post',
        'data': {
            'quote': "Leisure is the mother of philosophy."},
        'status_code': 200,
        'regexes': [
            r"Leisure", ], },
    {
        'name': "HTTP HEAD request",
        'path': '/imprimatur/test_01.html',
        'method': 'head',
        'status_code': 200, }]
