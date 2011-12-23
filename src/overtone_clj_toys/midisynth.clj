(ns overtone-clj-toys.midisynth
  (:use [overtone.live]))

(defsynth tdt-synth-env [freq 440.0
                         amp 0.8
                         pan-rate 0.5
                         filter 7
                         gate 1.0]
  (let [pan-ug (sin-osc:kr (lin-lin:kr pan-rate 0 127 0.0 10.0))
        lfo-rate pan-ug
        lfo-pan pan-ug
        filter-f (lin-lin:kr filter 0 127 100 5000)
        freq-a (/ freq 2)               ; Octave below
        freq-b (/ (* (/ 3 2) freq) 2)   ; Octave Below, 5th above
        freq-c (* (/ 4 3) freq)         ; Perfect 4th
        basef-mod (lin-lin:kr (lf-saw:kr lfo-rate) -1.0 1.0 0 10)
        mud (lin-lin:kr (lf-noise0:kr 15) -1 1 -5 5)
                                        ; Adds a some wobble.
        basef freq                      ; c/p no mouse modification
        signal (+ (* 0.8 (sin-osc (+ basef freq-a basef-mod)))
                  (* 0.5 (square  (+ mud basef freq-b basef-mod)))
                  (* 0.7 (saw     (+ mud basef freq-c basef-mod))))
;        env (env-gen (adsr 0.5 2 1 1) :gate (line 1 0 1) :action FREE)
        env (env-gen (adsr 0.5 2 1 1) :gate gate :action FREE)]
    (out 0 :signals (* env (pan2
                            (* amp (lpf:ar signal filter-f))
                            :pos (* 0.75 lfo-pan) ; Reduce Panning
                            )))))

;;
;; This string varies by device, use the zero param call
;; below to use the popup dialog.
;;
(def kb (midi-in "Keystation"))
;; 
;;(def kb (midi-in))

(def midi-log* (ref []))
(def controls* (ref {})) ; A Ref of currently active controls.
(def notes* (ref {}))    ; A Ref of currently sounding notes.

;; Map of MIDI note to Control Param
;; This will vary by keyboard / device.
(def control-map
  {74 :filter
   2  :pan-rate
   })

;; Calls our synth with whatever control parameters
;; have been stored in the controls ref.
(defn call-synth [note]
  (tdt-synth-env
   :freq (midi->hz note)
   :filter (get @controls* :filter 7)
   :pan-rate (get @controls* :pan-rate 2)))

;; Applies the current control and value to every
;; currently stored/sounding note.
(defn control-controls
  "Controls the control parameters."
  [control value]
  (ctl (doseq [x (vals @notes*)] (ctl x (control-map control) value))))


;;
;; MIDI listening function.
(defn midi-listener
  [event ts]
  (if (not (= :active-sensing (event :status)))
    (try
      (dosync (alter midi-log* conj event))
      (println event)
      (condp = (:cmd event)
        :control-change (let [control (:note event)
                              value (:data2 event)]
                          (dosync (alter controls*
                                         assoc (control-map control) value))
                          (control-controls control value))
        :note-on (let [note (:note event)
                       id   (get @notes* note)]
                   (if id (ctl id :gate 0))
                   (dosync (alter notes* assoc note
                                 (call-synth note))))
        :note-off (do
                    (ctl (get @notes* (:note event)) :gate 0)
                    (dosync (alter notes* dissoc (:note event))))
        true)
      (catch java.lang.Exception e (println "midi-listener exception: \n" e)))))

;; Set MIDI event handler
(midi-handle-events kb #'midi-listener)

