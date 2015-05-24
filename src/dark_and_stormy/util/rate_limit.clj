(ns dark-and-stormy.util.rate-limit
  (:import (java.util Date)))

(defn now-millis []
  (.getTime (Date.)))

(defn older-than? [now then]
  (> now then))

(defn millis-ago [millis]
  (- (now-millis) millis))

(defn remove-oldest [min-age coll]
  (vec (drop-while (partial > min-age) coll)))

(defn remove-older-than [max-millis coll]
  (remove-oldest (millis-ago max-millis) coll))

(defn rate-limit
  "Wraps `f` with a rate limiter, permitting at most `max` calls to
  `f` within the last `interval-msecs`. If `max` is exceeded, the
  returned function will throw an exception."
  [f max interval-msecs]
  (let [call-timestamps (atom [])]
    (fn [& args]
      (swap! call-timestamps (partial remove-older-than interval-msecs))
      (if (< (count @call-timestamps) max)
        (do
          (swap! call-timestamps conj (now-millis))
          (apply f args))
        (throw (ex-info "Rate limit exceeded."
                        {:wait-for-millis (- (first @call-timestamps)
                                             (millis-ago interval-msecs))}))))))
