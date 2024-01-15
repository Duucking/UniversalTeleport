import os
import socket
import time

from util import configutil
from util.sendnotification import send_notification


def send_tcp_message(address, port, send_data):
    tcp_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server_addr = (address, port)
    tcp_socket.connect(server_addr)
    tcp_socket.send(send_data.encode("UTF-8"))
    print("server: " + address + " send_data: " + send_data)
    tcp_socket.close()


def listen_tcp_message():
    tcp_server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    address = ('', 8556)
    tcp_server_socket.bind(address)
    tcp_server_socket.listen(128)
    client_socket, client_addr = tcp_server_socket.accept()
    recv_data = client_socket.recv(1024)
    print(
        "client_ip: " + str(client_addr[0]) + " client_port: " + str(
            client_addr[1]) + " recv_data: " + recv_data.decode("UTF-8"))
    client_socket.close()
    tcp_server_socket.close()
    if recv_data.decode("UTF-8") == "funtion:fileTrans":
        recv_file()
    return recv_data.decode("UTF-8")


def send_udp_broadcast(message, port=8557):
    ip_address = socket.gethostbyname(socket.gethostname())
    ip_address = ip_address.split('.')
    ip_address[3] = '255'
    broadcast_address = ip_address[0] + '.' + ip_address[1] + '.' + ip_address[2] + '.' + ip_address[3]
    address = (broadcast_address, port)
    udp_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    udp_socket.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1)
    udp_socket.sendto(message.encode("UTF-8"), address)
    print('udp broadcast message send ok !')
    udp_socket.close()


def listen_udp_message(port=8557):
    address = ('', port)
    udp_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    udp_socket.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1)
    udp_socket.bind(address)
    recv_data, client_addr = udp_socket.recvfrom(10240)
    print(
        "client_ip: " + str(client_addr[0]) + " client_port: " + str(
            client_addr[1]) + " recv_data: " + recv_data.decode("UTF-8"))
    udp_socket.close()
    if client_addr[0] == socket.gethostbyname(socket.gethostname()):
        return client_addr[0], ""
    return client_addr[0], recv_data.decode("UTF-8")


def send_file(filepath):
    ip_addr = configutil.read_config("Option", "ipAddress");
    send_tcp_message(ip_addr, 8556, "funtion:fileTrans")
    filepath = filepath
    filesize = str(os.path.getsize(filepath))
    fname1, fname2 = os.path.split(filepath)
    print(fname1, fname2)
    f = open(filepath, 'rb')
    tcp_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    tcp_socket.connect((ip_addr, 8558))
    print("connect success")
    tcp_socket.send(filesize.encode("UTF-8"))
    tcp_socket.send("\n".encode("UTF-8"))
    print("filesize :" + filesize)
    start = time.time()
    tcp_socket.send(fname2.encode("UTF-8"))
    tcp_socket.send("\n".encode("UTF-8"))
    print("fname2 :" + fname2)
    msg = tcp_socket.recv(1024)
    if msg.decode("UTF-8") == "received\n":
        while True:
            filedata = f.read(20480)
            if not filedata:
                break
            tcp_socket.send(filedata)
        print("send end")
        msg = tcp_socket.recv(1024)
        print("recv" + msg.decode("UTF-8"))
    tcp_socket.close()
    end = time.time()
    print('cost' + str(round(end - start, 2)) + 's')


def recv_file():
    tcp_server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    tcp_server_socket.bind(('', 8558))
    tcp_server_socket.listen(1)
    client_socket, client_addr = tcp_server_socket.accept()
    print('Accept new connection from %s:%s...' % (client_addr[0], client_addr[1]))
    data = client_socket.recv(1024)
    file_total_size = int(data.decode("UTF-8"))
    print("file_total_size:" + str(file_total_size))
    received_size = 0
    data = client_socket.recv(1024)
    filename = str(data.decode()).replace('\n', '')
    print("file_name" + filename)
    client_socket.send('received\n'.encode("UTF-8"))
    download_path = os.path.join(os.path.expanduser("~"), 'Downloads') + "\\UniversalTeleport\\"
    if os.path.isdir(download_path):
        pass
    else:
        os.makedirs(download_path)
    if os.path.exists(download_path + filename):
        end_index = filename.rfind('.')
        filename_list = list(filename)
        filename_list.insert(end_index, str(int(time.time())))
        filename = ''.join(filename_list)
    f = open(download_path + filename, 'wb')
    print("start receive")
    while received_size < file_total_size:
        data = client_socket.recv(1024)
        f.write(data)
        received_size += len(data)
    f.close()
    client_socket.send('finish\n'.encode("UTF-8"))
    send_notification("文件保存成功", download_path + filename)
    print("out while")
    client_socket.close()
