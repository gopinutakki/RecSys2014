'''
Created on Jul 15, 2014

@author: behnoush
'''
from extract_features import *
import extract_test as et


res = extract_features('training.dat','training.csv')

all_line_json_features,X_test,Y_test = et.extract_features_test('test.dat')

#logreg = build_model(train)
logreg = et.build_model(X_test,Y_test)


predictions = et.test_model(logreg,X_test)


