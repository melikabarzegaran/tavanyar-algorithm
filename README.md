# Tavanyar (algorithm)

Tavanyar is a system which detects and evaluates physical therapy exercises automatically using motion tracking devices.
This project holds the core algorithm of the system. It uses both dynamic time warping and subsequence dynamic time
warping to do the pattern matching. Different kinds of optimization techniques, such as sakoe-chiba band, and keogh lower
bounding techniques are used to make the algorithm of O(n) in terms of both performance and memory usage. Finally, we
introduce 9 parameters so that optimizing them, optimizes the overall functionality of the system in terms of accuracy,
performance, and memory usage.

## Parameters

- Distance function
- Local weights
- Global constraint width Factor (sakoe-chiba band technique)
- Generalization strategy (to multi-dimensional cases)
- Down sampling step
- Length tolerance factor
- Interpolation strategy
- Lower bounding radius (keogh lower bounding technique)
- Cost threshold

## References

1. Yurtman, A. and Barshan, B., 2013. Detection and evaluation of physical therapy exercises by dynamic time warping using wearable motion sensor units. In *Information Sciences and Systems 2013* (pp. 305-314). Springer, Cham.
2. Yurtman, A. and Barshan, B., 2014. Automated evaluation of physical therapy exercises using multi-template dynamic time warping on wearable sensor signals. *Computer methods and programs in biomedicine, 117(2)*, pp.189-207.
3. Yurtman, A., 2012. *Recognition and classification of human activities using wearable sensors* (Doctoral dissertation, bilkent university).
4. Berndt, D.J. and Clifford, J., 1994, July. Using dynamic time warping to find patterns in time series. In *KDD workshop* (Vol. 10, No. 16, pp. 359-370).
5. Müller, M., 2007. Dynamic time warping. *Information retrieval for music and motion*, pp.69-84.
6. Ratanamahatana, C.A. and Keogh, E., 2004, August. Everything you know about dynamic time warping is wrong. In *Third workshop on mining temporal and sequential data* (Vol. 32). Citeseer.
7. Sakoe, H., Chiba, S., Waibel, A. and Lee, K.F., 1990. Dynamic programming algorithm optimization for spoken word recognition. *Readings in speech recognition, 159*, p.224.
8. Shokoohi-Yekta, M., Hu, B., Jin, H., Wang, J. and Keogh, E., 2017. Generalizing DTW to the multi-dimensional case requires an adaptive approach. *Data mining and knowledge discovery, 31(1)*, pp.1-31.
9. Keogh, E. and Ratanamahatana, C.A., 2005. Exact indexing of dynamic time warping. *Knowledge and information systems, 7(3)*, pp.358-386.
10. Rath, T.M. and Manmatha, R., 2002. Lower-bounding of dynamic time warping distances for multivariate time series. *University of Massachusetts Amherst, Tech. Rep*. MM-40.
11. Fu, A.W.C., Keogh, E., Lau, L.Y., Ratanamahatana, C.A. and Wong, R.C.W., 2008. Scaling and time warping in time series querying. *The VLDB Journal—The International Journal on Very Large Data Bases, 17(4)*, pp.899-921.
12. Sokolova, M. and Lapalme, G., 2009. A systematic analysis of performance measures for classification tasks. *Information Processing & Management, 45(4)*, pp.427-437.