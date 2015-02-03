from flask import Flask, request, redirect, make_response, render_template
import sys
import traceback
from six import text_type
import threading
import imprimatur

app = Flask(__name__)

procs = {}

lock = threading.Lock


class ProcThread(threading.Thread):
    def __init__(self, script, **kwargs):
        threading.Thread.__init__(self, **kwargs)
        self.script = script
        self.results = []

    def run(self):
        for txt in imprimatur.run(self.script):
            try:
                lock.acquire()
                self.results.append(txt)
            finally:
                lock.release()


@app.route('/', methods=['GET', 'POST'])
def hello_world():
    if request.method == 'GET':
        return render_template('home.html')
    else:
        fl = request.files['file']
        script = text_type(fl.stream.read(), 'utf8')
        proc = ProcThread(script)
        lock.acquire()
        proc_id = len(procs)
        procs[proc_id] = proc
        lock.release
        proc.start()

        return render_template('home.html', script=script)


@app.route('/text_1')
def test_1():
    response = make_response('The best of all possible worlds.')
    response.headers['content-length'] = '32'
    response.headers['content-type'] = 'text; charset=utf-8'
    response.headers['date'] = 'Sat, 17 Jan 2015 13:30:54 GMT'
    response.headers['server'] = 'Werkzeug/0.9.6 Python/3.4.0'
    return response


@app.route('/text_2')
def test_2():
    return 'I warn you Mr Bond, my patience is not inexhaustible.'


@app.route('/echo', methods=['POST'])
def echo():
    ret = []
    fls = list(request.files.items())
    if len(fls) > 0:
        fname, fval = fls[0]
        ret.append(fname + ": " + text_type(fval.stream.read(), 'utf8') + '\n')
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
        response = make_response('authorized')
    else:
        response = make_response('not authorized', 403)
    response.headers['content-length'] = '14'
    response.headers['content-type'] = 'text; charset=utf-8'
    response.headers['date'] = 'Sat, 17 Jan 2015 13:30:54 GMT'
    response.headers['server'] = 'Werkzeug/0.9.6 Python/3.4.0'
    return response

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
