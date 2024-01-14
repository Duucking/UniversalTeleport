import os
import sys
import time
from threading import Thread

from service.clipboardlistener import clipboard_listener
from service.tcplistener import tcp_listener
from service.udplistener import udp_listener

tcp_listener_thread = Thread(target=tcp_listener)
udp_listener_thread = Thread(target=udp_listener)
clipboard_listener_thread = Thread(target=clipboard_listener)
thread_list = [tcp_listener_thread, udp_listener_thread, clipboard_listener_thread]
listener_list = [tcp_listener, udp_listener, clipboard_listener]


def guard_service():
    # 每60s检查一次子线程，如果线程挂掉，重新启动线程
    while True:
        for i in thread_list:
            if not i.is_alive():
                print(i.name + " is dead, restarting...")
                # restart()
                i = Thread(target=listener_list[thread_list.index(i)])
                i.start()
            else:
                print(i.name + " is alive")
        time.sleep(60)


if __name__ == '__main__':
    print('server start init!')
    tcp_listener_thread.start()
    udp_listener_thread.start()
    clipboard_listener_thread.start()
    # guard_thread = Thread(target=guard_service)
    # guard_thread.start()
    print('server init success!')

