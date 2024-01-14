from PyQt5.QtWidgets import *

from util import configutil
from util.datautil import AES_CBC_encrypt
from util.socketutil import send_tcp_message, send_udp_broadcast


def clipboard_listener():
    print("clipboard_listener start!")
    app = QApplication([])
    clipboard = app.clipboard()

    # 当剪切板变动会执行该方法
    def change_deal():
        data = clipboard.mimeData()
        print("clipbaord changed! new content: " + data.text())
        key = configutil.read_config("Option", "privateKey")
        send_udp_broadcast(AES_CBC_encrypt(data.text(), key), 8557)

    # 监听剪切板变动
    clipboard.dataChanged.connect(change_deal)
    app.exec_()
