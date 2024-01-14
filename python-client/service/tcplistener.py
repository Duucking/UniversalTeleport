from util import configutil
from util.clipboardutil import setClipboard
from util.datautil import AES_CBC_decrypt
from util.socketutil import listen_tcp_message


def tcp_listener():
    print("tcp_listener start!")
    while True:
        data = listen_tcp_message()
        if data == "funtion:fileTrans":
            pass
        elif data != "":
            key = configutil.read_config("Option", "privateKey")
            data = AES_CBC_decrypt(data, key)
            setClipboard(data)
