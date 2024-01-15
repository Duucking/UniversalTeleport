import os

from util import configutil
from util.clipboardutil import setClipboard
from util.datautil import AES_CBC_decrypt
from util.sendnotification import send_notification
from util.socketutil import listen_tcp_message


def tcp_listener():
    print("tcp_listener start!")
    while True:
        data = listen_tcp_message()
        if data == "funtion:fileTrans":
            pass
        elif data != "":
            key = configutil.read_config("Option", "privateKey")
            if key != "":
                data = AES_CBC_decrypt(data, key)
                setClipboard(data)
                title = '已复制到剪切板'
                message = data[0:16] + '...'
                send_notification(title, message)
