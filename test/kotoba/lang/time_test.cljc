(ns kotoba.lang.time-test
  (:require [clojure.test :refer [deftest is testing]]
            [kotoba.lang.time :as t]))

(deftest epoch
  (is (= "1970-01-01T00:00:00Z" (t/->iso8601 (t/instant 0))))
  (is (= "1970-01-01T00:00:01Z" (t/->iso8601 (t/instant 1000))))
  (is (= "1970-01-01T00:01:00Z" (t/->iso8601 (t/instant 60000))))
  (is (= "1970-01-01T01:00:00Z" (t/->iso8601 (t/instant 3600000))))
  (is (= "1970-01-02T00:00:00Z" (t/->iso8601 (t/instant 86400000)))))

(deftest a-known-date
  (is (= "2026-06-30T12:00:00Z" (t/->iso8601 (t/iso8601->instant "2026-06-30T12:00:00Z"))))
  (is (= "2000-01-01T00:00:00Z" (t/->iso8601 (t/iso8601->instant "2000-01-01T00:00:00Z"))))
  (is (= "1999-12-31T23:59:59Z" (t/->iso8601 (t/iso8601->instant "1999-12-31T23:59:59Z")))))

(deftest roundtrip
  (doseq [ms [0 1000 60000 3600000 86400000 1751289600000 4102444800000]]
    (is (= ms (:time/instant (t/iso8601->instant (t/->iso8601 (t/instant ms)))))
        (str "roundtrip failed for ms=" ms))))

(deftest arithmetic
  (is (t/before? (t/instant 1000) (t/instant 2000)))
  (is (t/after?  (t/instant 2000) (t/instant 1000)))
  (is (= (:time/instant (t/add (t/instant 1000) (t/seconds 1))) 2000))
  (is (= (:time/duration (t/sub (t/instant 2000) (t/instant 1000))) 1000)))

(deftest now-uses-injected-clock
  (is (= (:time/instant (t/now (fn [] 1750000000000))) 1750000000000)))

(deftest duration-helpers
  (is (= (:time/duration (t/seconds 3)) 3000))
  (is (= (:time/duration (t/minutes 2)) 120000))
  (is (= (:time/duration (t/hours 1)) 3600000))
  (is (= (:time/duration (t/days 1)) 86400000)))

(deftest parse-rejects-malformed
  (is (thrown? #?(:clj clojure.lang.ExceptionInfo :cljs js/Error) (t/iso8601->instant "short")))
  (is (thrown? #?(:clj clojure.lang.ExceptionInfo :cljs js/Error) (t/iso8601->instant "2026-06-30 12:00:00Z")))) ; space not T
