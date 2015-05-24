(ns dark-and-stormy.util.stats)

(defn sample
  "Given a distribution's mean, generates a sample integer greater
  than or equal to zero distributed around that mean."
  [mean]
  (+ (rand-int mean) (rand-int mean)))
