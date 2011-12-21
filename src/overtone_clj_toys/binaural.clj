(ns overtone-clj-toys.binaural
  (:use [overtone.live]))

;;
;; Binaural Beat Synthesis:
;; Generates binaural beats given the provided carrier and desired
;; frequency. Brown noise is used to soften the background and
;; block out outside noise.

;; freq    effect
;;   < 4   Delta, Sleep
;; 3 - 7   Theta, relaxation, meditation
;; 7 - 13  Alpha, Relaxation while Awake
;;
(defsynth bbeat [amp 0.3
                 carrier 440
                 freq 4.5]
  (let [freq-a carrier
        freq-b (+ carrier freq)
        env (lin-lin (sin-osc:kr (* freq (/ 1 60))) -1.0 1.0 0.5 1.0)
        left (+ (* 0.8 (sin-osc freq-a)) (* env (brown-noise)))
        right (+ (* 0.8 (sin-osc freq-b)) (* env (brown-noise)))]
    (out 0 (* amp left))
    (out 1 (* amp right))))

;;(bbeat 0.2 440 3)
;;(stop)

