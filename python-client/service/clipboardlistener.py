from PyQt5.QtWidgets import *

from util.socketutil import send_tcp_message


def clipboard_listener():
    print("clipboard_listener start!")
    app = QApplication([])
    clipboard = app.clipboard()

    # 当剪切板变动会执行该方法
    def change_deal():
        data = clipboard.mimeData()
        print("clipbaord changed! new content: "+data.text())
        send_tcp_message(data.text())

    # 监听剪切板变动
    clipboard.dataChanged.connect(change_deal)
    app.exec_()
