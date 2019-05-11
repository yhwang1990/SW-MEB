# SW-MEB #

A Java implementation of the algorithms in KDD'19 paper "Coresets for Minimum Enclosing Balls over Sliding Windows", an extended version of which is available on [arXiv](https://arxiv.org/abs/1905.03718).

## Requirements ##

JDK 8 or newer, no third-party libraries required

## Usage ##

### 1. Input Format ###

A dataset is stored in one `.txt` file. Each line of the file represents one point in the dataset. Different dimensions of the point are split by a single space.

```csv
    <p_1> <p_2> ...... <p_m>
    <q_1> <q_2> ...... <q_m>
    ......
```

We upload an examplar dataset with size `n=1M` and dimension `m=10` in folder `/data`.
A few samples of the dataset are as follows:

```csv
    0.8 -0.9 2.08 0.76 0.98 -1.68 -0.03 0.12 -0.39 -0.64
    0.05 0.52 -0.82 0.26 -0.45 1.4 0.27 -0.01 0.9 0.86
    0.37 0.4 0.06 0.94 0.44 -0.73 -0.01 -0.16 -0.58 -0.21
    ......
```

### 2. How to Run the Code ###

To run an algorithm for MEB in Euclidean space:

```shell
    $ java -jar run-meb.jar <alg_name> <dataset_dir> <size> <N> <m> <eps>
    alg_name:    String, the algorithm name (AOMEB, BBC, CoreMEB, DynMEB, SSMEB, SWMEB, SWMEB+)
    dataset_dir: String, the path of the dataset file
    size:        int, the number of points in the dataset file
    N:           int, the window size
    m:           int, the dimension of points
    eps:         float, the parameter epsilon or epsilon_1 (omitted for SSMEB)
```

For example,  

```shell
    java -jar -Xmx80000m run-meb.jar AOMEB data/synthetic-1000000-10.txt 1000000 100000 10 0.001
```

or

```shell
    java -jar -Xmx80000m run-meb.jar SSMEB data/synthetic-1000000-10.txt 200000 100000 10
```

To run an algorithm for kernelized MEB in RKHS:

```shell
    $ java -jar run-kernel-meb.jar <alg_name> <dataset_dir> <size> <N> <m> <eps>
    alg_name:    String, the algorithm name (AOMEB, BBC, CoreMEB, DynMEB, SSMEB, SWMEB, SWMEB+)
    dataset_dir: String, the path of the dataset file
    size:        int, the number of points in the dataset file
    N:           int, the window size
    m:           int, the dimension of points
    eps:         float, the parameter epsilon or epsilon_1 (omitted for SSMEB)
```

Note that the gamma value (kernel width) of each dataset is stored in `/data/gamma.txt` and read before running the algorithm.

For example,  

```shell
    java -jar -Xmx80000m run-kernel-meb.jar AOMEB data/synthetic-1000000-10.txt 200000 100000 10 0.0001
```

or

```shell
    java -jar -Xmx80000m run-kernel-meb.jar SSMEB data/synthetic-1000000-10.txt 200000 100000 10
```

### 3. Output Format ###  

```csv
    <alg_name>
    <dataset_dir> <N> <m> <eps>
    <idx>
    radius=<the radius of the coreset's MEB> (or the radius of approximate MEB returned by SSMEB)
    cpu_time=<CPU time> (time per update for AOMEB, BBC, CoreMEB, DynMEB, SSMEB; total update time for SWMEB, SWMEB+)
    coreset_size=<the size of the coreset>
    num_points=<the number of points stored by SWMEB/SWMEB+>
    meb_radius=<the radius of approximate MEB>
```

## Contact ##

If there is any question, feel free to contact: [Yanhao Wang](mailto:yanhao90@comp.nus.edu.sg).