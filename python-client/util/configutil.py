from configparser import ConfigParser


def read_config(section, key):
    conf = ConfigParser()
    conf.read('config.ini', "UTF-8")
    return conf[section][key]
