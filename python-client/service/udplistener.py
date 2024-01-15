import base64
import hashlib
import os
import socket

from util import configutil
from util.clipboardutil import setClipboard
from util.datautil import AES_CBC_decrypt
from util.sendnotification import send_notification
from util.socketutil import listen_udp_message, send_tcp_message


def udp_listener():
    print("udp_listener start!")
    while True:
        target_ip, data = listen_udp_message()
        key = configutil.read_config("Option", "privateKey")
        if key != "":
            device_name = configutil.read_config("Option", "deviceName")
            if data == "funtion:deviceDiscover":
                local_ip = socket.gethostbyname(socket.gethostname())
                sha256hash = hashlib.sha256()
                sha256hash.update(key.encode("UTF-8"))
                key = sha256hash.hexdigest()
                device_data = ('{"deviceType":"Windows","IP":"' + local_ip + '","deviceName":"'
                               + device_name + '","Key":"' + key + '"}')
                send_tcp_message(target_ip, 8559, device_data)
                pass
            elif data != "":
                data = AES_CBC_decrypt(data, key)
                setClipboard(data)
                title = '已复制到剪切板'
                message = data[0:16] + '...'
                send_notification(title, message)
