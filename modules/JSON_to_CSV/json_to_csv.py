#!/usr/bin/env python

import json
import sys
import re

__author__ = 'gopi'


uniquekeys = set()
features = dict()
csvfilename = ''


def add_features(sep, value):
    global features
    if sep in features:
        try:
            features[sep] = features[sep], value
        except UnicodeEncodeError:
            return
    else:
        features[sep] = value


def get_all_keys(data, sep="~"):
    global uniquekeys
    global features

    uniquekeys.add(sep)

    if isinstance(data, list):
        if len(data) == 0:
            #print sep, ' None'
            #features[sep] = 'None'
            add_features(sep, 'None')
        for item in data:
            get_all_keys(item, sep)
    elif isinstance(data, dict):
        for k, v in data.iteritems():
            if k == 'indices':
                continue
            get_all_keys(v, sep+'~'+k)
    else:
        #print sep, data
        #features[sep] = data
        add_features(sep, data)
        sep = '~'


def print_header():
    csvfile = open(csvfilename, 'w')
    for k in uniquekeys:
        csvfile.write(k + '\t')
    csvfile.write('\n')
    csvfile.close()


def get_key_value(line_json_features, k):
    if line_json_features.get(k) is None:
        return ''
    else:
        try:
            return str(line_json_features.get(k)).replace("u\'", "").replace("\'", "")
        except UnicodeEncodeError:
            return ''


def print_csv(line_json_features):
    csvfile = open(csvfilename, 'a')
    ftr = ''
    for k in uniquekeys:
        ftr = ftr + '\"' + get_key_value(line_json_features, k) + '\"\t'

    ftr.replace("\\r\\n", " ")
    ftr.replace("\\n", " ")
    ftr.replace(",", " ")
    csvfile.write(ftr + '\n')
    csvfile.close()


def generate_csv(all_line_json_features):
    print_header()
    for line_json_features in all_line_json_features:
        print_csv(line_json_features)


def main(json_input_file):
    global features
    all_line_json_features = []
    all_line_json = []
    f = open(json_input_file, "r")
    next(f)
    for line in f:
        tokens = line.split(',', 4)

        uniquekeys.add('twitter_user_id')
        uniquekeys.add('imdb_item_id')
        uniquekeys.add('rating')
        uniquekeys.add('scraping_timestamp')
        features['twitter_user_id'] = tokens[0]
        features['imdb_item_id'] = tokens[1]
        features['rating'] = tokens[2]
        features['scraping_timestamp'] = tokens[3]

        line_json = json.loads(tokens[4])
        all_line_json.append(line_json)
        get_all_keys(line_json, "~")
        all_line_json_features.append(features.copy())
        features.clear()

    uniquekeys.remove("~")
    uk = sorted(uniquekeys, key=None)
    print len(uk)
    print len(all_line_json_features)
    generate_csv(all_line_json_features)
    print 'Done!'

if __name__ == '__main__':

    if len(sys.argv) != 3:
        print 'USAGE: python json_to_csv <input JSON file> <output CSV file>'
    csvfilename = sys.argv[2]
    main(sys.argv[1])
