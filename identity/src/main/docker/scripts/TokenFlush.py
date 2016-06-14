#!/usr/bin/python

import commands

class FlushTokens:
    def __init__(self):
        pass

    def flush_expired_tokens(self):
        commands.getstatusoutput('keystone-manage -v token_flush')

def main():
    flush_tokens = FlushTokens()
    flush_tokens.flush_expired_tokens()

if __name__ == "__main__":
    main()
