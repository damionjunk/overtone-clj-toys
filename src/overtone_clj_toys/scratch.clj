;; Scratch pad for playing with ideas. The state of this file may vary
;; drastically, it is not intended for public consumption, despite its
;; presence on GitHub.
(ns overtone-clj-toys.scratch
  (:require [incanter.core :as ico]
            [incanter.charts :as ich]
            [incanter.datasets :as ida])
  (:use [overtone.live]))


(def b (buffer 44100))
(def a (buffer 44100))
(def c (buffer 44100))

(defsynth fetch-a [] (record-buf (saw 440) a :action FREE :loop 0))
(defsynth fetch-data []
  (record-buf (trig (sine 1) 1) b :action FREE :loop 0))

(fetch-data)
(fetch-a)
(fa)


(def d (buffer-data b))
(def e (buffer-data a))
(def f (buffer-data c))

(def waveplot (ich/xy-plot
                (range 500)
                (take 500 d)
                :title "Sine Wave"
                :x-label "Time"
                :y-label "Amplitude"))
(ico/view waveplot)
(ich/add-lines waveplot (range 500) (take 500 e))
(ich/add-lines waveplot (range 500) (take 500 f))


(definst beep [] (sin-osc:ar 30))
(definst beep [] (* 0.5 (+ (square 60) (sin-osc 100))))




