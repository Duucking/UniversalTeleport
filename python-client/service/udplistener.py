import base64
import hashlib
import socket

from util import configutil
from util.clipboardutil import setClipboard
from util.datautil import AES_CBC_decrypt
from util.socketutil import listen_udp_message, send_tcp_message


def udp_listener():
    print("udp_listener start!")
    while True:
        target_ip, data = listen_udp_message()
        key = configutil.read_config("Option", "privateKey")
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