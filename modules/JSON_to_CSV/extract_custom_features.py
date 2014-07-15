#!/usr/bin/env python

import json
import sys


__author__ = 'gopi'


uniquekeys = set()
user_avg_ratings = dict()
mov_avg_ratings = dict()
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
    csvfile.write(' '.join(ftr.splitlines()) + '\n')
    csvfile.close()


def generate_csv(all_line_json_features):
    print_header()
    for line_json_features in all_line_json_features:
        print_csv(line_json_features)


def generate_custom_csv(all_line_json_features):
    csvfile = open('custom_features_1.csv', 'w')
    csvfile.write('twitter_user-id\timdb_item_id\trating\tscraping_timestamp\tavg_user_rating\t\
        avg_movie_rating\t~~user~friends_count\t~~user~favourites_count\n')
    for line_json_features in all_line_json_features:
        ftr = str(line_json_features['twitter_user_id']) + '\t' + str(line_json_features['imdb_item_id']) + '\t' + str(line_json_features['rating']) + '\t' + str(line_json_features['scraping_timestamp']) + '\t' + str(get_avg_user_rating(line_json_features['twitter_user_id'])) + '\t' + str(get_avg_movie_rating(line_json_features['imdb_item_id'])) + '\t' + str(line_json_features['~~user~friends_count']) + '\t' + str(line_json_features['~~user~favourites_count']) + '\n'
        csvfile.write(ftr)
    csvfile.close()


def collect_user_ratings(twitter_user_id, rating):
    if twitter_user_id in user_avg_ratings:
        user_avg_ratings[twitter_user_id].append(rating)
    else:
        user_avg_ratings[twitter_user_id] = [rating]


def get_avg_user_rating(t_user_id):
    return sum(user_avg_ratings[t_user_id])/len(user_avg_ratings[t_user_id])


def get_avg_movie_rating(item_id):
    return sum(mov_avg_ratings[item_id])/len(mov_avg_ratings[item_id])


def collect_movie_ratings(imdb_item_id, rating):
    if imdb_item_id in mov_avg_ratings:
        mov_avg_ratings[imdb_item_id].append(rating)
    else:
        mov_avg_ratings[imdb_item_id] = [rating]


def main(json_input_file):
    global features, user_avg_ratings, mov_avg_ratings

    all_line_json_features = []
    all_line_json = []
    f = open(json_input_file, "r")
    next(f)
    for line in f:
        line.replace('\t', ' ')
        tokens = line.split(',', 4)

        uniquekeys.add('twitter_user_id')
        uniquekeys.add('imdb_item_id')
        uniquekeys.add('rating')
        uniquekeys.add('scraping_timestamp')
        features['twitter_user_id'] = tokens[0]
        features['imdb_item_id'] = tokens[1]
        features['rating'] = float(tokens[2])
        features['scraping_timestamp'] = tokens[3]

        collect_user_ratings(features['twitter_user_id'], features['rating'])
        collect_movie_ratings(features['imdb_item_id'], features['rating'])

        line_json = json.loads(tokens[4])
        all_line_json.append(line_json)
        get_all_keys(line_json, "~")
        all_line_json_features.append(features.copy())
        features.clear()

    uniquekeys.remove("~")
    uk = sorted(uniquekeys, key=None)
    print len(uk)
    print len(all_line_json_features)
    #generate_csv(all_line_json_features)
    generate_custom_csv(all_line_json_features)
    print 'Done!'

if __name__ == '__main__':
    csvfilename = sys.argv[2]
    main(sys.argv[1])
