#!/bin/bash
java -jar RankLib.jar -train full_60_training_SVM.dat -test full_60_01_testing_UIDs_In_Training_SVM_1.dat -ranker 8 -metric2t NDCG@10 -metric2T NDCG@10 -save model.txt
java -jar RankLib.jar -load model.txt -rank full_60_01_testing_UIDs_In_Training_SVM_1.dat -score score.txt
