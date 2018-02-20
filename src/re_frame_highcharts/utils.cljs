(ns re-frame-highcharts.utils
  (:require [reagent.core :as reagent]))

; Highcharts wants to maintain its own instance, with mutating state.
; So we'll need to break from our lovely pure world and manage these.
; The below atom holds a map of these chart instances, keys by a chart id.
(defonce chart-instances (atom {}))
(defonce stock-instances (atom {}))

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
                (if (:redo chart-meta)
                  (swap! chart-instances dissoc chart-id))
                (if-let [chart-instance (get @chart-instances chart-id)]
                  (doall (map (partial update-series chart-instance) (:series chart-data)))
                  (mount-chart this))))]
      (reagent/create-class {:reagent-render       render-chart
                             :component-did-mount  mount-chart
                             :component-did-update update-chart}))))

(defn stock
  [{:keys [chart-meta]}]
  (let [style (or (:style chart-meta) {:height "100%" :width "100%"})]
    (letfn [(render-chart
              []
              [:div {:style style}])
            (mount-chart
              [this]
              (let [[_ {:keys [chart-meta chart-data post-update-fn]}] (reagent/argv this)
                    chart-id (:id chart-meta)
                    chart-instance (js/Highcharts.StockChart. (reagent/dom-node this)
                                                              (clj->js chart-data))]
                (swap! stock-instances assoc chart-id chart-instance)
                (when post-update-fn (post-update-fn chart-instance))
                chart-instance))
            (update-series
              [chart-instance {:keys [id data] :as series-options}]
              ; if it's the first time, chart.get(<series id>) will return nil
              ; so we need to add the series instead
              (if-let [series (.get chart-instance id)]
                (-> series
                    (.setData (clj->js data)))
                (-> chart-instance
                    (.addSeries (clj->js series-options)))))
            (update-chart
              [this]
              (let [[_ {:keys [chart-meta chart-data post-update-fn]}] (reagent/argv this)
                    chart-id (:id chart-meta)
                    chart-instance (if-let [ci (get @stock-instances chart-id)]
                                     ci
                                     (mount-chart this))]
                (if (:redo chart-meta)
                  (swap! stock-instances dissoc chart-id))
                (doseq [s (:series chart-data)]
                  (update-series chart-instance s))
                (when post-update-fn
                  (post-update-fn chart-instance))))]
      (reagent/create-class {:reagent-render       render-chart
                             :component-did-mount  mount-chart
                             :component-did-update update-chart}))))
