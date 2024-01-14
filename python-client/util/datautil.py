import base64

from Crypto.Cipher import AES
from Crypto import Random
import hashlib

from Crypto.Util.Padding import pad


def AES_CBC_encrypt(text, key):
    key = MD5_encrypt(key)
    mode = AES.MODE_CBC
    pad_text = pad(text.encode(), 16, style='pkcs7')
    cryptos = AES.new(key.encode(), mode, MD5_encrypt("114514").encode())
    cipher_text = cryptos.encrypt(pad_text)
    print(cipher_text)
    cipher_text = base64.b64encode(cipher_text)
    return cipher_text.decode()


# AES-CBC解密
def AES_CBC_decrypt(text, key):
    key = MD5_encrypt(key)
    text = base64.b64decode(text)
    print(text)
    mode = AES.MODE_CBC
    cryptos = AES.new(key.encode(), mode, MD5_encrypt("114514").encode())
    plain_text = cryptos.decrypt(text)
    return plain_text.decode(errors="ignore")


def MD5_encrypt(key):
    md5 = hashlib.md5()
    md5.update(key.encode("UTF-8"))
    return (md5.hexdigest())[8:-8].lower()
