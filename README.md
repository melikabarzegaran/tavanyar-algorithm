# Tavanyar (algorithm)

Tavanyar is a system which detects and evaluates physical therapy exercises automatically using motion tracking devices.
This project holds the core algorithm of the system. It uses multi-template multi-match dynamic time warping algorithm, 
which is a natural extension of subsequence dynamic time warping algorithm. Finally, we introduce 5 parameters so that 
optimizing them, optimizes the overall functionality of the system in terms of accuracy, performance, and memory usage.

## Parameters

- Distance function
- Local weights
- Length tolerance factor
- Overlapping factor
- Cost threshold

## References

1. Yurtman, A. and Barshan, B., 2013. Detection and evaluation of physical therapy exercises by dynamic time warping using wearable motion sensor units. In *Information Sciences and Systems 2013* (pp. 305-314). Springer, Cham.
2. Yurtman, A. and Barshan, B., 2014. Automated evaluation of physical therapy exercises using multi-template dynamic time warping on wearable sensor signals. *Computer methods and programs in biomedicine, 117(2)*, pp.189-207.
3. Yurtman, A., 2012. *Recognition and classification of human activities using wearable sensors* (Doctoral dissertation, bilkent university).
4. Berndt, D.J. and Clifford, J., 1994, July. Using dynamic time warping to find patterns in time series. In *KDD workshop* (Vol. 10, No. 16, pp. 359-370).
5. Müller, M., 2007. Dynamic time warping. *Information retrieval for music and motion*, pp.69-84.
6. Shokoohi-Yekta, M., Hu, B., Jin, H., Wang, J. and Keogh, E., 2017. Generalizing DTW to the multi-dimensional case requires an adaptive approach. *Data mining and knowledge discovery, 31(1)*, pp.1-31.
7. Sokolova, M. and Lapalme, G., 2009. A systematic analysis of performance measures for classification tasks. *Information Processing & Management, 45(4)*, pp.427-437.