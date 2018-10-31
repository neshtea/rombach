(ns rombach.structure.product-test
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.test.alpha :as stest]
            [clojure.test :refer :all]
            [rombach.structure.product :refer [defproduct]]
            [rombach.control.lens :as lens]))

(s/def ::name (s/and string? #(< 0 (count %))))
(s/def ::age pos-int?)

(defproduct person make-person person?
  [[first ::name]
   [last ::name]
   [age ::age]])

(defproduct phone-number make-phone-number phone-number?
  [[number string?]])

(defproduct contact make-contact contact?
  [[person ::person]
   [phone-number ::phone-number]])

(defproduct phone-book make-phone-book phone-book?
  [[contacts (s/coll-of ::contact :into [])]])

(deftest constructor-test
  (testing "constructing with the correct parameters works"
    (is (= {:_struct 'person
            ::person-first "Marco"
            ::person-last "Schneider"
            ::person-age 29}
           (make-person "Marco" "Schneider" 29))))
  (testing "fails if specs do not match if constructor is instrumented"
    (stest/instrument)
    (is (thrown? clojure.lang.ExceptionInfo (make-person "Marco" :schneider 29))))
  (testing "works fine if not instrumented"
    (stest/unstrument)
    (is (= {:_struct 'person
            ::person-first "Marco"
            ::person-last :schneider
            ::person-age 29}
           (make-person "Marco" :schneider 29)))))

(stest/instrument)

(deftest accessor-test
  (let [marco (make-person "Marco" "Schneider" 29)]
    (is (= "Marco" (person-first marco)))
    (is (= "Schneider" (person-last marco)))
    (is (= 29 (person-age marco)))))

(deftest lens-view-test
  (let [marco (make-person "Marco" "Schneider" 29)]
    (is (= "Marco" (lens/_view person-first-lens marco)))
    (is (= "Schneider" (lens/_view person-last-lens marco)))
    (is (= 29 (lens/_view person-age-lens marco))))
  (testing "fails when specs not satisfied"
    (is (thrown? clojure.lang.ExceptionInfo
                 (lens/_view person-age-lens (make-person "Marco" "Schneider" :29))))))

(deftest lens-update-test
  (let [marco (make-person "Marco" "Schneider" 29)]
    (is (= "Heinrich" (person-first (lens/_update person-first-lens marco "Heinrich"))))
    (is (= "Rombach" (person-last (lens/_update person-last-lens marco "Rombach"))))
    (is (= 95 (person-age (lens/_update person-age-lens marco 95)))))
  (testing "fails when specs not satisfied"
    (is (thrown? clojure.lang.ExceptionInfo
                 (lens/_update person-age-lens (make-person "Marco" "Schneider" :29) 95)))))

(deftest lens-over-test
  (let [marco (make-person "Marco" "Schneider" 29)
        scream #(str % "!")]
    (is (= "Marco!" (person-first (lens/_over person-first-lens marco scream))))
    (is (= "Schneider!" (person-last (lens/_over person-last-lens marco scream))))
    (is (= 30 (person-age (lens/_over person-age-lens marco inc))))
    (testing "fails when specs not satisfied"
      (is (thrown? clojure.lang.ExceptionInfo
                   (lens/_over person-first-lens (make-person "Marco" "Schneider" :29) scream))))))

(deftest predicate-test
  (let [marco (make-person "Marco" "Schneider" 29)]
    (is (person? marco))
    (is (not (person? 42)))))

  (let [phone-book (make-phone-book [(make-contact (make-person "Marco" "Schneider" 29)
                                                   (make-phone-number "0123456789"))
                                     (make-contact (make-person "Heinrich" "Rombach" 95)
                                                   (make-phone-number "9876543210"))])]
    (= 29
       (lens/_view (lens/>> phone-book-contacts-lens
                            (lens/list!! 0)
                            contact-person-lens
                            person-age-lens)
                   phone-book))
    ;; Set Marco's age to 30.
    (= (make-phone-book [(make-contact (make-person "Marco" "Schneider" 30)
                                       (make-phone-number "0123456789"))
                         (make-contact (make-person "Heinrich" "Rombach" 95)
                                       (make-phone-number "9876543210"))]))
    (lens/_update (lens/>> phone-book-contacts-lens
                           (lens/list!! 0)
                           contact-person-lens
                           person-age-lens)
                  phone-book
                  30))

(deftest complex-lens-test
  (let [phone-book (make-phone-book [(make-contact (make-person "Marco" "Schneider" 29)
                                                   (make-phone-number "0123456789"))
                                     (make-contact (make-person "Heinrich" "Rombach" 95)
                                                   (make-phone-number "9876543210"))])]
    (is (= 29
           (lens/_view (lens/>> phone-book-contacts-lens
                                (lens/list!! 0)
                                contact-person-lens
                                person-age-lens)
                       phone-book)))
    ;; Set Marco's age to 30.
    (is (= (make-phone-book [(make-contact (make-person "Marco" "Schneider" 30)
                                           (make-phone-number "0123456789"))
                             (make-contact (make-person "Heinrich" "Rombach" 95)
                                           (make-phone-number "9876543210"))])
           (lens/_update (lens/>> phone-book-contacts-lens
                                  (lens/list!! 0)
                                  contact-person-lens
                                  person-age-lens)
                         phone-book
                         30)))
    ;; Increment Marco's age.
    (is (= (make-phone-book [(make-contact (make-person "Marco" "Schneider" 30)
                                           (make-phone-number "0123456789"))
                             (make-contact (make-person "Heinrich" "Rombach" 95)
                                           (make-phone-number "9876543210"))])
           (lens/_over (lens/>> phone-book-contacts-lens
                                (lens/list!! 0)
                                contact-person-lens
                                person-age-lens)
                       phone-book
                       inc)))))

(deftest validate!-test
  (let [marco (make-person "Marco" "Schneider" 29)]
    (is (validate-person! marco))
    (is (thrown? clojure.lang.ExceptionInfo
                 (validate-person! (assoc (make-person "Marco" "Schneider" 29)
                                          ::person-age
                                          -1))))
    (is (thrown? clojure.lang.ExceptionInfo
                 (validate-person! 42)))))
