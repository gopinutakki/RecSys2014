#!/usr/bin/env python

from imdb import IMDb
from imdbpie import Imdb
from multiprocessing.pool import ThreadPool
import multiprocessing
import cPickle as pickle
import sys


imdb_statement = IMDb()
imdb_more = Imdb()
movie_ids = set()
saved_movie_ids = set()
movie_information = dict()
pickle_lock = multiprocessing.Lock()
counter = 0

class IMDBMovies(object):

    def __init__(self, mid):
        self.store_movie_information(mid)


    def store_movie_information(self, movie):
        global movie_information, saved_movie_ids, counter
        global pickle_lock

        print movie, len(movie_information),

        if movie in saved_movie_ids:
            return

        m = dict()
        m['mid'] = movie
        m['recommendations'] = imdb_statement.get_movie_recommendations(movie)
        m['main'] = imdb_statement.get_movie_main(movie)
        m['keywords'] = imdb_statement.get_movie_keywords(movie)
        m['full_credits'] = imdb_statement.get_movie_full_credits(movie)
        m['release_dates'] = imdb_statement.get_movie_release_dates(movie)

        imdb_movie_for_update = imdb_statement.get_movie(movie)
        imdb_statement.update(imdb_movie_for_update, info=('vote details',))
        m['update'] = imdb_movie_for_update

        m['more'] = imdb_more.find_movie_by_id('tt'+movie)

        pickle_lock.acquire()
        try:
            movie_information[movie] = m
            if len(movie_information) % 100 == 0:
                pickle.dump(movie_information, open(str(counter) + '-imdb.p', 'wb'))
                counter += 1
                movie_information.clear()
                print '--> Saved'
        except:
            print "Was unable to acquire the lock"
            raise
        finally:
            pickle_lock.release()
            print ' done.'

def get_movie_id(fname):
    f = open(fname,'r')
    for line in f.readlines():
        #id = line.split(',')[1] # use this line for the raw dataset.
        mid = line.strip()  # use this for the file with the movie_ids alone.
        movie_ids.add(mid)
    if 'item_id' in movie_ids:
        movie_ids.remove('item_id')
    #print len(movie_ids)


def get_movie_information():
    global movie_information

    print 'Fetching IMDB data from counter: ', counter
    pool = ThreadPool(processes=10)
    pool.map(IMDBMovies, movie_ids)
    #m_infos = pool.map(IMDB_Movies, movie_ids)
    #m = [m_info.get_movie_infor() for m_info in m_infos]
    pickle.dump(movie_information, open(str(counter)+'-imdb.p', 'wb'))


def read_movie_ids_from_pickles():
    global saved_movie_ids, movie_ids, counter
    print 'Reading the pickles to get all the saved movie IDs'
    c = 0
    while True:
        try:
            c += 1
            movies = pickle.load(open(str(c) + '-imdb.p', 'rb'))
            for k in movies.keys():
                saved_movie_ids.add(k)
            print 'Done for pickle: ', c
        except:
            print "Done reading saved movie pickles."
            # Do not raise exception here to facilitate continued execution
            break
        finally:
            counter = c
    print "Total saved movies: %s, out of %s" % (len(saved_movie_ids), len(movie_ids))
    movie_ids.difference_update(saved_movie_ids)

if __name__ == '__main__':
    for arg in sys.argv[1:]:
        get_movie_id(arg)
        read_movie_ids_from_pickles()
        get_movie_information()
