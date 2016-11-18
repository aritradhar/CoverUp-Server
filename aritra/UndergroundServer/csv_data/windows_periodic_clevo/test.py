import pdb
import numpy as np
from streampie import *

from sklearn import ensemble
from sklearn import utils
from sklearn import cross_validation
from sklearn.metrics import accuracy_score

from matplotlib import pyplot as plt


# returns np.array of floats
def parse(f_name):
   lines = open(f_name).read().strip().split("\n")
   return [float(i) for i in lines]

def process(samples, chunk_size):
   X = []
      
   chunks = samples.reshape((len(samples)/chunk_size, chunk_size))

   for chunk in chunks:
      bins, _ = np.histogram(chunk, np.arange(980, 1020, 0.1))
      
      bins = bins.astype(float)
      data = bins/np.sum(bins)

      X.append(data)

   return np.array(X)

def process_data(data):
   X = []

   for chunk in data:
      bins, _ = np.histogram(chunk, np.arange(980, 1020, 0.5))
      
      bins = bins.astype(float)
      data = bins/np.sum(bins)

      X.append(data)

   return np.array(X)

def genenerate_mixed_samples(noise_ratio, chunk_size, test_flat_X_0, test_flat_X_1):
    chunk_size_test_x1 = int(noise_ratio*chunk_size)
    chunk_size_test_x0 = chunk_size - chunk_size_test_x1 # ensures total of chunk_size

    test_Xh = []

    i_0 = 0
    i_1 = 0
    while i_0 < (len(test_flat_X_0) - chunk_size_test_x0 ) and i_1 < (len(test_flat_X_1) - chunk_size_test_x1):
        sample = np.append( test_flat_X_0[i_0:i_0 + chunk_size_test_x0], test_flat_X_1[i_1:i_1 + chunk_size_test_x1])
        sample_hist = process(sample, chunk_size)
        test_Xh.append(sample_hist[0]) 

        i_0 += chunk_size_test_x0
        i_1 += chunk_size_test_x1

    #print ""
    #print "used %d of X_0 (%d left)" % (train_size_0*chunk_size + i_0, len(test_flat_X_0) - i_0)
    #print "used %d of X_1 (%d left)" % (train_size_1*chunk_size + i_1, len(test_flat_X_1) - i_1)
    #print "generated (%d, %d) training samples and %d testing samples" % (train_Xh_0.shape[0], train_Xh_1.shape[0], len(test_Xh))

    return (np.array(test_Xh), i_0, i_1)

def metric(y_test, y_res):
   # How many one and zero cases are there?
   cnt_1 = len(y_test[y_test == 1])
   cnt_0 = len(y_test[y_test == 0])

   tmp = y_test[np.where(y_res == 1)]

   pr1 = len(tmp[tmp == 1]) / float(cnt_1)
   pr2 = len(tmp[tmp == 0]) / float(cnt_0)

   return abs(pr1 - pr2)



# has to be less than x0_size / 2*chunk_size


#train_Xh_0 = process(X_0[0: train_size_0*chunk_size])
#train_Xh_1 = process(X_1[0: train_size_1*chunk_size])



#assume no shuffeling since creation of k-staples

#
if False:
    bins = np.arange(980, 1020, 0.1)
    #bins1 = np.arange(2030, 2100 , 0.1)
    #bins2 = np.arange(2030, 2100 , 0.1)
    bins = bins[:-1]
    fig, ax = plt.subplots(6)
    for i in np.arange(0,3):
        ax[i].bar(bins, test_Xh_0[i], width = 0.1, color='r')
    for i in np.arange(0,3):
        ax[i+3].bar(bins, test_Xh_1[i], width = 0.1, color='r')

    plt.show()

    sys.exit(0)

#
# TRAINING
#

# Concatenate the positive and the negative training samples
#X = np.concatenate((train_Xh_0, train_Xh_1), axis=0)
#y = np.concatenate((np.zeros((train_Xh_0.shape[0],)), np.ones((train_Xh_1.shape[0],))), axis=0)


# Shuffle them around, for good measure

# Train the random forrest on all the samples available to the attacker

#
# TESTING
#
print ""
#print len(test_Xh_0)
#print len(test_Xh_1)

file = open("/dev/random","r")
import struct
seed = struct.unpack("i", file.read(4))[0] & 0x7fffffff
np.random.seed(seed)

X_0_fresh = np.array(parse("woExt.csv"))
print "read 0 samples: %d " % len(X_0_fresh)
#X_0_fresh = X_0_fresh + np.random.normal(0, 3, (len(X_0_fresh)))

X_1_fresh = np.array(parse("withExt.csv"))
print "read 1 samples: %d" % len(X_1_fresh)
#X_1_fresh = X_1_fresh + np.random.normal(0, 3, (len(X_1_fresh)))

res = []
# noise_ratio: how much interactive samples are in the training set

try:
    raise
    import json
    with open('data.json', 'r') as infile:
        res = json.load(infile)
except:
    print "\t%s\t\t%s\t\t%s\t\t%s\t\t%s" % ("ratio", "tn", "fn", "tp", "fp")
    if True:
    #for fixed_size_0 in [1, 50, 100, 300, 500]: 
     for test_chunk_size in [20, 40 ,60, 80, 100, 120, 200, 300, 400, 500, 1000, 2000, 5000]:
      for train_size in [20]:
       #for test_chunk_size in [40, 80, 120, 250, 500]: 
        for noise_ratio in np.append(np.arange(0, 1, 0.03), np.array(1)) :
          print "Frame %d %d %f" % (test_chunk_size, train_size, noise_ratio)
          try:
            #fixed_size = 
            train_chunk_size = 1500
            train_chunk_size_0 = train_chunk_size
            train_chunk_size_2 = train_chunk_size
            train_size_0 = train_size
            train_size_2 = train_size
            #get maximal sample size
            X_0 = X_0_fresh
            X_1 = X_1_fresh
            
            np.random.shuffle(X_0)
            np.random.shuffle(X_1)

            #pdb.set_trace()

            train_Xh_0 = process(X_0[0:train_chunk_size_0*train_size_0], train_chunk_size_0)
            X_0 = X_0[train_chunk_size_0*train_size_0:]

            # get a 50 % fixed len
            #fixed_size_0 = 0;
            if noise_ratio == 0.0:
                fixed_size_0 = len(X_0)/(2*test_chunk_size)
            elif noise_ratio == 1.0:
                fixed_size_0 = min(len(X_0),len(X_1))/test_chunk_size
            else:
                t1 = float(len(X_1))/noise_ratio/test_chunk_size # num test chunks that can be built from X_1
                #t2 = float(len(X_0)) / (2 * test_chunk_size) / ( 1 - noise_ratio) # X_0 neded
                #t2 = float(len(X_0)) /(2 - noise_ratio) * ( 1 - noise_ratio) / test_chunk_size # fixed_size_0 of X_0
                t2 = float(len(X_0) ) / (test_chunk_size * (2 - noise_ratio)) # fixed_size_0 of X_0
                #t3 = t2 /(1-noise_ratio) # num test chunks of X_0
                if t2 > t1:
                    fixed_size_0 = t1
                else:
                    fixed_size_0 = t2
            fixed_size_0 = int(fixed_size_0)

            #print "test %d %d " %(, fixed_size_0)
            X_0_fixed = process(X_0[0:test_chunk_size*fixed_size_0],test_chunk_size)
            X_0 = X_0[fixed_size_0*test_chunk_size:]

            (Xh_2, i_0, i_1) = genenerate_mixed_samples(noise_ratio, test_chunk_size, X_0, X_1)
            train_Xh_2 = Xh_2[0:train_size_2]
            test_Xh_2 = Xh_2[train_size_2:]

            #if len(test_Xh_2) > fixed_size_0:
            print "Warning 50%% not fullfilled: %d %d" % (  fixed_size_0, len(test_Xh_2))
            # fitting
            X = np.concatenate((train_Xh_2, train_Xh_0), axis=0)
            y = np.concatenate((np.ones((train_Xh_2.shape[0],)), np.zeros((train_Xh_0.shape[0],))), axis=0)
            X, y = utils.shuffle(X, y)
            model = ensemble.RandomForestClassifier(n_estimators=300, max_features=20, max_depth=20)
            model.fit(X, y)

            print X_0_fixed.shape

            print test_Xh_2.shape
            test_Xh_mix = np.concatenate((X_0_fixed, test_Xh_2), axis=0)

            # prediction
            p = model.predict(test_Xh_mix)

            # metric
            #corr_mix = np.concatenate((np.zeros((mix_size_0,)), np.ones((mix_size_1,))), axis=0)
            y0   = p[0 : fixed_size_0]
            ymix = p[fixed_size_0 : ]

            
            tn = float(len( y0 [ y0 == 0 ] ) ) / len(y0)
            fp = float(len( y0 [ y0 == 1 ] ) ) / len(y0) # = 1 - tn

            tp = float(len( ymix [ ymix == 1] ) ) / len(ymix)
            fn = 1 - tp

            tmp  = (i_0/test_chunk_size, i_1/test_chunk_size, noise_ratio, tn, fn, tp, fp, tn - fn)
            #print "%d %d\t%f\t%f\t%f\t%f\t%f\t%f" % tmp

            res.append((fixed_size_0, len(test_Xh_2), test_chunk_size, train_chunk_size, train_size, noise_ratio, tn, fp, tp, fn))
          except Exception as e:
            print "invalid: " + str(e) #+ str((fixed_size_0, train_size, chunk_size, noise_ratio))

         
        
        #guess = float(len(pred_y[pred_y == 1])) / len(pred_y) 
        #print guess 

        #res.append([noise_ratio, guess])


# TODO: Create special files that contain the right mixture of noise + data
# X_test = process("special_file.csv")

import json
# test case
with open("data/data_%d.json" % seed, 'w') as outfile:
    json.dump(res, outfile)

with open('data.json', 'w') as outfile:
    json.dump(res, outfile)

res_tmp = []
for b in res:
    res_tmp.append(tuple(b))
res = res_tmp
res = np.array(res, dtype=[('fixed_size_abs', 'i4'), ('mix_len', 'i4'), ('test_chunk_size', 'i4'), ('test_train_size', 'i4'), ('train_size', 'i4'), ('noise_ratio', 'f4'),('tn', 'f4'), ('fp', 'f4'), ('tp', 'f4'),('fn','f4') ] )

for key in res.dtype.fields.keys():
    pass
    #print key + ": " + str(np.unique(res[key]))

from matplotlib import pyplot as plt

res = np.array(res)
cla = np.unique(res['test_chunk_size'])
plot = []
for s in cla:
    #plot.append(res[res[:,1] == s])
    da_set = np.all([res['test_chunk_size'] == s ], axis=0)
    p = res[da_set]
    plt.yticks(np.arange(0.4, 1, 0.025))
    plt.grid()
    plt.plot(p['noise_ratio'], ((np.abs(p['tn'] - p['fn']) + 1)/2), label=str(s))
plt.xlabel("purity")
plt.ylabel("accuracy")
plt.title("Different test chunk sizes")
plt.legend(loc='upper left')
plt.show()

sys.exit(0)









# round measurement:
res_tmp = []
for b in res:
    res_tmp.append(tuple(b))
res = res_tmp
res = np.array(res, dtype=[('fixed_size_abs', 'i4'), ('mix_len', 'i4'), ('chunk_size', 'i4'), ('train_size', 'i4'), ('noise_ratio', 'f4'),('tn', 'f4'), ('fp', 'f4'), ('tp', 'f4'),('fn','f4') ] )

for key in res.dtype.fields.keys():
    print key + ": " + str(np.unique(res[key]))

from matplotlib import pyplot as plt

res = np.array(res)
cla = np.unique(res['chunk_size'])
plot = []
for s in cla:
    #plot.append(res[res[:,1] == s])
    da_set = np.all([ res['test_chunk_size'] == s ], axis=0)
    p = res[da_set]
    plt.yticks(np.arange(0.4, 1, 0.025))
    plt.grid()
    plt.plot(p['noise_ratio'], ((np.abs(p['tn'] - p['fn']) + 1)/2), label=str(s))
plt.xlabel("noise_ratio")
plt.ylabel("accuracy")
plt.title("Different chunk size")
plt.legend(loc='upper left')
plt.show()

sys.exit(0)

# other measurement
cla = np.unique(res[:,0])
plot = []
for s in cla:
    #plot.append(res[res[:,1] == s])
    p = res[res[:,0] == s]
    plt.plot( p[:,2],p[:,1], label=str(s))
plt.ylabel("noise_ratio")
plt.xlabel("tn -fn")
plt.title("N")
plt.legend(loc='upper left')
plt.show()

print ""
print ""

sys.exit(0)


