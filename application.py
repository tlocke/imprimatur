from flask import Flask, request, redirect
import sys
import traceback

app = Flask(__name__)


@app.after_request
def set_fixed_date(response):
    response.headers['Date'] = 'Sat, 17 Jan 2015 13:30:54 GMT'
    return response


@app.route('/')
def hello_world():
    return 'Hello World!'


@app.route('/text_1')
def test_1():
    return 'The best of all possible worlds.'


@app.route('/text_2')
def test_2():
    return 'I warn you Mr Bond, my patience is not inexhaustible.'


@app.route('/echo', methods=['POST'])
def echo():
    ret = []
    fls = list(request.files.items())
    if len(fls) > 0:
        fname, fval = fls[0]
        ret.append(fname + ": " + str(fval.stream.read(), 'utf8') + '\n')
    ret.append(str(request.form) + '\n')
    return ''.join(ret)


@app.route('/redirect')
def redir():
    location = request.args['location']
    return redirect(location)


@app.route('/auth')
def auth():
    auth = request.authorization
    if auth is not None and auth.username == 'conrad' and \
            auth.password == 'kurtz':
        return 'authorized'
    else:
        return 'not authorized', 403

count = 0


@app.route('/counter_reset')
def counter_reset():
    global count
    count = 0
    return "Counter reset.\n"


@app.route('/counter')
def counter():
    global count
    count += 1
    if count == 3:
        return "The Decline And Fall Of The Roman Empire\n"
    else:
        return str(count) + "Gibbon\n"


@app.errorhandler(500)
def page_not_found(e):
    error = traceback.format_exc()
    sys.stderr.write(error)
    return error, 500

if __name__ == '__main__':
        app.run()
