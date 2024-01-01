import sys

from util import socketutil, configutil

if __name__ == '__main__':
    print("hello world")
    filepath = sys.argv[1]
    ip = configutil.read_config("Option", "ipAddress")
    print(ip, filepath)
    socketutil.send_file(ip, filepath)
