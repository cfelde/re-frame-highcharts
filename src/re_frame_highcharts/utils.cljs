(ns re-frame-highcharts.utils
  (:require [reagent.core :as reagent]))

; Highcharts wants to maintain its own instance, with mutating state.
; So we'll need to break from our lovely pure world and manage these.
; The below atom holds a map of these chart instances, keys by a chart id.
(defonce chart-instances (atom {}))

(defn chart
  [{:keys [chart-meta]}]
  (let [style (or (:style chart-meta) {:height "100%" :width "100%"})]
    (letfn [(render-chart
              []
              [:div {:style style}])
            (mount-chart
              [this]
              (let [[_ {:keys [chart-meta chart-data]}] (reagent/argv this)
                    chart-id (:id chart-meta)
                    chart-instance (js/Highcharts.Chart. (reagent/dom-node this)
                                                         (clj->js chart-data))]
                (swap! chart-instances assoc chart-id chart-instance)))
            (update-series
              [chart-instance {:keys [id data]}]
              (-> chart-instance
                  (.get id)
                  (.setData (clj->js data))))
            (update-chart
              [this]
              (let [[_ {:keys [chart-meta chart-data]}] (reagent/argv this)
                    chart-id (:id chart-meta)]
                (if-let [chart-instance (get @chart-instances chart-id)]
                  (doall (map (partial update-series chart-instance) (:series chart-data))))))]
      (reagent/create-class {:reagent-render render-chart
                             :component-did-mount mount-chart
                             :component-did-update update-chart}))))
