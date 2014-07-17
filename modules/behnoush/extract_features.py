'''
Created on Jul 15, 2014

@author: behnoush
'''
#!/usr/bin/env python

import json
import sys
from sklearn import linear_model
import numpy as np


__author__ = 'gopi'


uniquekeys = set()
user_avg_ratings = dict()
mov_avg_ratings = dict()
features = dict()
#all_line_json_features=[]

#csvfilename = 'behnoush_features_1.csv'
#csvfilename = sys.argv[2]


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



def generate_custom_csv(all_line_json_features,the_solution_file):
    lines = list()
    lines.append('twitter_user-id , imdb_item_id , rating , scraping_timestamp , avg_user_rating ,\
        avg_movie_rating , ~~user~friends_count , ~~user~followers_count , ~~user~favourites_count\n')
    for line_json_features in all_line_json_features:
        line = str(line_json_features['twitter_user_id']) + ',' + str(line_json_features['imdb_item_id']) + ',' + str(line_json_features['rating']) + ',' + str(line_json_features['scraping_timestamp']) + ',' + str(get_avg_user_rating(line_json_features['twitter_user_id'])) + ',' + str(get_avg_movie_rating(line_json_features['imdb_item_id'])) + ',' + str(line_json_features['~~user~friends_count']) + ',' + str(line_json_features['~~user~followers_count']) + ',' + str(line_json_features['~~user~favourites_count']) + '\n'
        lines.append(line)
    # Prepare the writing...
    with file(the_solution_file,'w') as outfile:
        outfile.writelines(lines)
    outfile.close()
    return lines


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

def build_model(all_line_json_features,C=0.01):
    X=[]
    Y=[]
    for line_json_features in all_line_json_features:
        feat=[]
        feat.append(line_json_features['twitter_user_id'])
        feat.append(line_json_features['imdb_item_id'])
        feat.append(line_json_features['rating'])
    #    feat.append(line_json_features['scraping_timestamp'])
        feat.append(get_avg_user_rating(line_json_features['twitter_user_id']))
        feat.append(get_avg_movie_rating(line_json_features['imdb_item_id']))
        feat.append(line_json_features['~~user~friends_count'])
        feat.append(line_json_features['~~user~followers_count'])
#        feat.append(line_json_features['~~user~favourites_count'])
        
        X.append(feat)
        Y.append(line_json_features['~~user~favourites_count'])
       
    logreg = linear_model.LogisticRegression(C=C)
    logreg.fit(np.array(X), np.array(Y))
   # predictions = logreg.test(np.array(X))
    #print predictions
    
    return logreg

def test_model(logreg,X_test):
    
    predictions = logreg.test(np.array(X_test))
    return predictions



def extract_features(json_input_file,the_solution_file):
    global features, user_avg_ratings, mov_avg_ratings

    all_line_json_features =[]
    
    
    
    all_line_json = []
    f = open(json_input_file, "r")
    next(f)
    for line in f:
     #   print 'here'
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
    #   print line_json['retweet_count']
    #    print line_json
        all_line_json.append(line_json)
        get_all_keys(line_json, "~")
   #print 'space'
    #print 'feature'
        all_line_json_features.append(features.copy())
        features.clear()

    uniquekeys.remove("~")
    uk = sorted(uniquekeys, key=None)
    print len(uk)
    print len(all_line_json_features)
#    build_model(all_line_json_features)
    csv_features = generate_custom_csv(all_line_json_features,the_solution_file)
    print 'Done!'
    return csv_features

#if __name__ == '__main__':
    #csvfilename = sys.argv[2]
    #main(sys.argv[1])
