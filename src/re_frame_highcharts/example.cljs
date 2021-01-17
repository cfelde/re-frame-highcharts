(ns re-frame-highcharts.example
  (:require [reagent.core :as reagent]
            [reagent.dom]
            [re-frame.core :as rf]
            [re-frame-highcharts.utils :as chart-utils]
            [re-frame-highcharts.example-data :as example-data]))

(def line-config
  {:chart {:type "line"}
   :plotOptions {:line {:animation false}}
   :legend {:layout "vertical"
            :align "right"
            :verticalAlign "top"
            :floating true
            :borderWidth 1
            :shadow true}
   :credits {:enabled false}
   :series [{:id "series-1"
             :name "Secords"
             :data []}]})

(def pie-config
  {:chart {:type "pie"}
   :plotOptions {:pie {:animation false}}
   :title {:text "A pie chart"}
   :series [{:id "series-1"
             :name "Test"
             :innerSize "80%"
             :data [{:name "Test 1"
                     :y 100}
                    {:name "Test 2"
                     :y 50}
                    {:name "Test 3"
                     :y 25}]}]})

(def stock-config
  {:rangeSelector {:selected 1}
   :title {:text "AAPL Stock Price"}
   :series [{:name "AAPL"
             :data example-data/stock-data
             :tooltip {:valueDecimals 2}}]})

;; -- Domino 1 - Event Dispatch -----------------------------------------------

(defn dispatch-timer-event
  []
  (let [s (.getSeconds (js/Date.))]
    (rf/dispatch [:timer s])))

(defonce do-timer (js/setInterval dispatch-timer-event 1000))

;; -- Domino 2 - Event Handlers -----------------------------------------------

(rf/reg-event-db
  :initialize
  (fn [_ _]
    {:chart-1 {:chart-meta {:id :chart-1
                            :style {:height "100%"
                                    :width "100%"}}
               :chart-data (assoc-in line-config [:title :text] "Last 120 seconds")}
     :chart-2 {:chart-meta {:id :chart-2
                            :style {:height "100%"
                                    :width "100%"}}
               :chart-data (assoc-in line-config [:title :text] "Last 60 seconds")}
     ; If you add a :redo true to the chart meta map, the
     ; chart will get recreated instead of updated.
     ; This is useful when you want to add or remove
     ; series or do other changes other than updating
     ; existing series.
     :chart-3 {:chart-meta {:id :chart-3
                            :redo true}
               :chart-data pie-config}

     ; Used when viewing Highstock chart
     :stock-1 {:chart-meta {:id :stock-1}
               :chart-data stock-config}}))

(rf/reg-event-db
  :timer
  (fn [db [_ s]]
    (let [db (update-in db [:chart-1 :chart-data :series 0 :data] #(into [] (take-last 120 (conj % s))))
          db (update-in db [:chart-2 :chart-data :series 0 :data] #(into [] (take-last 60 (conj % s))))]
      db)))

;; -- Domino 4 - Query  -------------------------------------------------------

(rf/reg-sub
  :value
  (fn [db [_ k]]
    (k db)))


;; -- Domino 5 - View Functions ----------------------------------------------

(defn chart-1
  []
  (let [data (rf/subscribe [:value :chart-1])]
    (fn []
      [chart-utils/chart @data])))

(defn chart-2
  []
  (let [data (rf/subscribe [:value :chart-2])]
    (fn []
      [chart-utils/chart @data])))

(defn chart-3
  []
  (let [data (rf/subscribe [:value :chart-3])]
    (fn []
      [chart-utils/chart @data])))

(defn stock-1
  []
  (let [data (rf/subscribe [:value :stock-1])]
    (fn []
      [chart-utils/stock @data])))

(defn charts-ui
  []
  [:div
   [:div [chart-1]]
   [:div [chart-2]]
   [:div [chart-3]]])

(defn stock-ui
  []
  [:div
   [:div [stock-1]]])

;; -- Entry Point -------------------------------------------------------------

(defn on-js-reload [])
;; optionally touch your app-state to force rerendering depending on
;; your application
;; (swap! app-state update-in [:__figwheel_counter] inc)

(defn ^:export run
  [type]
  (rf/dispatch-sync [:initialize])
  (case type
    "charts" (reagent.dom/render [charts-ui] (js/document.getElementById "app"))
    "stock"  (reagent.dom/render [stock-ui]  (js/document.getElementById "app"))))
