import sys

from util import socketutil, configutil

if __name__ == '__main__':
    filepath = sys.argv[1]
    socketutil.send_file(filepath)
