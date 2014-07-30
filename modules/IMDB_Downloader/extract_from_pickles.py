__author__ = 'gpc94156'


from imdb import IMDb
from imdbpie import Imdb
from multiprocessing.pool import ThreadPool
import multiprocessing
import cPickle as pickle
import sys
import json

def read_movie_from_pickles(dir_path):
    print 'Reading the pickles to get all the saved movie IDs'
    c = 0
    f = open('imdb_features.csv', 'wb')
    while True:
        c += 1
        path = dir_path + str(c) + '-imdb.p'
        try:
            movies = pickle.load(open(path, 'rb'))
        except:
            break

        print 'Path: ', path
        for k in movies.keys():
            ge = ''
            cs = ''
            ds = ''
            rd = ''
            try:
                if movies[k]['more'].genres is not None:
                    for gen in movies[k]['more'].genres:
                        ge += gen + " "

                if movies[k]['more'].cast_summary is not None:
                    for person in movies[k]['more'].cast_summary:
                        cs += person.imdb_id + " "

                if movies[k]['more'].release_date is not None:
                    rd = movies[k]['more'].release_date

                if movies[k]['more'].directors_summary is not None:
                    for dir in movies[k]['more'].directors_summary:
                        ds += dir.imdb_id + " "

            except:
                print "ERROR: ", movies[k]['mid']
            finally:
                m_summary = movies[k]['mid'] + '\t' + ge + '\t' + cs + '\t' + rd + '\t' + ds + '\n'
                f.write(m_summary)
    f.close()

if __name__ == '__main__':
    read_movie_from_pickles('C:\\Users\\WKUUSER\\Documents\\pickles\\')
