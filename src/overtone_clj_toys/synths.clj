(ns overtone-clj-toys.synths
  (:use [overtone.live]))

;; Tuned-Detuned Synth
;;
;; Mouse X direction controls the base frequency
;; Mouse Y direction controls the LFO which controls the saw tooth
;;       output that modifies the base frequency slightly.
;;
;; Mouse Y also controls a LPF on the mixed audio signal.
;;
;;
(defsynth tdt-synth [freq 440
                     amp 0.8
                     pan-rate 0.5]
  (let [lfo-rate (mouse-y 0 30)
        lfo-pan (sin-osc:kr pan-rate)
        freq-a (/ freq 2)               ; Octave below
        freq-b (/ (* (/ 3 2) freq) 2)   ; Octave Below, 5th above
        freq-c (* (/ 4 3) freq)         ; Perfect 4th
        basef-mod (lin-lin:kr (lf-saw:kr lfo-rate) -1.0 1.0 0 10)
        mud (lin-lin:kr (lf-noise0:kr 15) -1 1 -5 5)
                                        ; Adds a some wobble.
        basef (mouse-x 50 1000)
        signal (+ (* 0.8 (sin-osc (+ basef freq-a basef-mod)))
                  (* 0.5 (square  (+ mud basef freq-b basef-mod)))
                  (* 0.7 (saw     (+ mud basef freq-c basef-mod))))]
    (out 0 :signals (pan2
                     (* amp (lpf:ar signal (mouse-y 500 4000 EXP)))
                     :pos (* 0.75 lfo-pan) ; Reduce Panning
                     ))))

;; View the output, it's pretty!
;; (scope :bus 0)
;; (scope :bus 1)

;; Move mouse to the upper left to hear the "base" sound.
;; Reminiscent of a Boards of Canada type drone synth
;;
;; (tdt-synth :freq 150 :amp 0.8 :pan-rate 0.24)
;; (stop)