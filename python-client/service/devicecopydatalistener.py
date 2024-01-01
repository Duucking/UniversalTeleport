from util.clipboardutil import setClipboard
from util.notifactionutil import send_notification
from util.socketutil import listen_tcp_message


def device_copy_data_listener():
    print("device_copy_data_listener start!")
    while True:
        data = listen_tcp_message()
        if data == "funtion:fileTrans":
            pass
        else:
            setClipboard(data)
