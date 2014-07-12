#!/usr/bin/env python

import json, sys, unicodecsv
__author__ = 'gopi'


def print_all_keys(data, indent=0):
    if isinstance(data, list):
        print
        for item in data:
            print_all_keys(item, indent+1)
    elif isinstance(data, dict):
        print
        for k, v in data.iteritems():
            print "    " * indent, k + ":",
            print_all_keys(v, indent + 1)
    else:
        print data


def main(json_input_file):
    f = open(json_input_file, "r")
    lines = [f.readline()]

    for line in lines:
        tokens = line.split(',', 4)
        line_json = json.loads(tokens[4])
        print(line_json['entities'])
        print_all_keys(line_json, 0)


if __name__ == '__main__':
    main('tr.txt')