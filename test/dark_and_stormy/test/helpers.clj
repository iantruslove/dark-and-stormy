(ns dark-and-stormy.test.helpers
  (:require [com.stuartsierra.component :as component]))

(defmacro with-system
  "Starts the component system definition passed, binding the running
  system. Evaluates all forms in that binding scope, then stops the
  system."
  [bindings & forms]
  (assert (= 2 (count bindings)))
  `(let [~(first bindings) (component/start-system ~(second bindings))]
     (try
       ~@forms
       (finally
         (component/stop-system ~(first bindings))))))
