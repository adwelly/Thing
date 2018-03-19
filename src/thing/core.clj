(ns thing.core
  (:require [clojure.string :as s]))

(declare str-horizontal)

(defn spaces [n] (apply str (repeat n " ")))

(defn non-homogeneous-comp [a b]
  (if (and (= (type a) (type b)) (instance? Comparable a))
    (compare a b)
    (compare (str a) (str b))))

(defn make-selection-arr [structure]
  (cond (map? structure) (into [] (sort non-homogeneous-comp (keys structure)))
        (set? structure) (into [] (sort non-homogeneous-comp structure))
        :else []))

(defn longer-than? [s n]
  (try
    (nth s n)
    true
    (catch IndexOutOfBoundsException e false)))

(defn str-vector-horizontal [v w]
  (if (< (count v) w)
    (str v " ")
    (let [l (into [] (map str-horizontal (take w v)))]
      (str "[" (apply str l) "...] "))))

(defn str-map-horizontal [m w]
  (if (< (count m) w)
    (str m " ")
    (let [subkeys (subvec (make-selection-arr m) 0 w)
          submap (select-keys m subkeys)
          arr (map #(str-horizontal %) (flatten (into [] submap)))]
      (str "{" (apply str arr) " ...} "))))

(defn str-set-horizontal [s w]
  (if (< (count s) w)
    (str s " ")
    (let [l (into [] (map str-horizontal (take w s)))]
      (str "#{" (apply str l) "...}"))))

(defn str-record-horizontal [r w]
  (let [len (count r)
        subkeys (subvec (make-selection-arr r) 0 (if (< len w) len w))
        submap (select-keys r subkeys)
        name (str (subs (str (type r)) 6))
        arr (map #(str-horizontal %) (flatten (into [] submap)))]
    (str name "{" (apply str arr) (if (< len w) "" "...") "} ")))

(defn str-seq-horizontal [s w]
    (let [v (take w s)
          len (count v)]
        (str "<" (apply str (into [] (map str-horizontal v))) (if (longer-than? s w) "..." "") ">")))

(defn str-list-horizontal [s w]
  (if (< (count s) w)
    (str s " ")
    (let [l (into [] (map str-horizontal (take w s)))]
      (str "(" (apply str l) "...)"))))

(defn str-horizontal [s]
  (cond (vector? s) (str-vector-horizontal s 4)
        (record? s) (str-record-horizontal s 4)
        (map? s) (str-map-horizontal s 4)
        (set? s) (str-set-horizontal s 4)
        (list? s) (str-list-horizontal s 4)
        (seq? s) (str-seq-horizontal s 4)
        (string? s) (str "\"" s "\" ")
        :else (str s " ")))

(defn print-vector-vertical [v]
  (let [len (count v)]
    (doseq [i (range len)] (println (str i ". " (if (zero? i) " [" "  ") (str-horizontal (get v i)) (if (= i (dec len)) "]" ""))))))

(defn print-map-vertical [m]
  (let [ks (make-selection-arr m)
        len (count ks)]
    (doseq [i (range len) :let [k (get ks i)]]
      (println (str i "." (if (zero? i) " {" "  ") (str-horizontal k) (str-horizontal (get m k)) (if (= i (dec len)) "}" ""))))))

(defn print-set-vertical [s]
  (let [vals (make-selection-arr s)
        len (count vals)]
    (doseq [i (range len) :let [v (get vals i)]]
      (println (str i "." (if (zero? i) " #{" "  ") (str-horizontal v) (if (= i (dec len)) "}" ""))))))

(defn print-record-vertical [r]
  (let [ks (make-selection-arr r)
        name (str (subs (str (type r)) 6))
        indent (spaces (+ (count name) 2))
        len (count ks)]
    (doseq [i (range len) :let [k (get ks i)]]
      (println (str i "." (if (zero? i) (str " " name "{") indent) (str-horizontal k) (str-horizontal (get r k)) (if (= i (dec len)) "}" ""))))))

(defn print-list-vertical [l]
  (let [len (count l)]
    (doseq [i (range len)]
      (println (str i ". " (if (zero? i) " (" "  ") (str-horizontal (nth l i)) (if (= i (dec len)) ")" ""))))))

(defn print-seq-vertical [v]
  (let [s (take 15 v)
        len (count s)]
    (doseq [i (range len)]
      (println (str i ". " (if (zero? i) " <" "  ") (str-horizontal (nth v i))
                    (if (= i (dec len))
                      (if (longer-than? v len) "...>" ">")
                      ""))))))


(defn pretty-print-vertical [s]
  (cond (vector? s) (print-vector-vertical s)
        (record? s) (print-record-vertical s)
        (map? s) (print-map-vertical s)
        (set? s) (print-set-vertical s)
        (list? s) (print-list-vertical s)
        (seq? s) (print-seq-vertical s)
        :else (println (str-horizontal s))))

(defn parse-int [s]
  (try (. Integer parseInt s) (catch Exception ex nil)))

(defn print-help []
  (println "\\x - exit.")
  (println "\\h - print this message.")
  (println ".. - pop the path.")
  (println "<num> - examine that part of the structure."))

(defn extract [structure selection selection-arr]
  (if (empty? selection-arr)
    (if (seq? structure) (nth structure selection)
                         (get structure selection))
    (get structure (get selection-arr selection))))

(defn exec [command path structures]
  (let [selection (parse-int command)
        structure nil
        selection-arr (make-selection-arr structure)]
    (cond (= command "\\x") nil
          (= command "\\h") (do (print-help) (list path structures))
          (= command "..") (if (empty? path) (list path structures) (list (pop path) (pop structures)))
          (and (some? selection) (coll? structure)) (list (conj path selection) (conj structures (extract structure selection selection-arr)))
          :else (list path structures))))

(defn debug
  ([structure] (debug "" structure))
  ([msg structure]
   (println msg)
   (loop [path [] structures [structure]]
     (pretty-print-vertical (last structures))
     (print path "-> ")
     (flush)
     (let [command (s/trim (s/lower-case (read-line)))
           [new-path new-structures] (exec command path structures)]
       (if (some? new-path)
         (recur new-path new-structures)
         (last structures))))))

