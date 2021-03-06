package de.tum.spark.ml;

import org.apache.spark.ml.clustering.KMeans;
import org.apache.spark.ml.clustering.KMeansModel;
import org.apache.spark.ml.feature.StandardScalerModel;
import org.apache.spark.ml.feature.VectorAssembler;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import org.apache.spark.ml.feature.StandardScaler;
import org.apache.spark.storage.StorageLevel;

import java.util.*;

import static java.util.Map.Entry.comparingByValue;
import static java.util.stream.Collectors.toMap;

public class SparkKMeansClustering {


    public static void main(String[] args) {

        SparkSession spark = SparkSession
                .builder()
                .appName("Clustering")
                .config("spark.master", "local[*]")
                .config("spark.driver.memory", "16g")
                .config("spark.default.parallelism", "8")
                .config("spark.driver.bindAddress", "127.0.0.1")
                .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
                .config("spark.kryoserializer.buffer.max","1g")
                // .config("spark.executor.memory", "32g")
                .getOrCreate();

        Dataset<Row> df = spark.read()
                .option("header", false)
                .option("inferSchema", true)
                //.option("partition", 4)
                .csv("/Users/coworker/Downloads/kddcup.data").cache();
        

        List<String> dropCols = new ArrayList<String>();

        /* These will be user inputs*/
        dropCols.add("_c1");
        dropCols.add("_c2");
        dropCols.add("_c3");

        //label column name..this will be user input
        String labelCol = "_c41";



        df = removeNonNumericFeatures(df, dropCols);
        Dataset<Row> target_df = df.drop(labelCol);
        df = df.drop(labelCol);

        for (String c : df.columns()) {
            if (c.equals(labelCol)) {
                continue;
            }
            df = df.withColumn(c, df.col(c).cast("double"));

        }
        VectorAssembler assembler = new VectorAssembler().setInputCols(target_df.columns()).setOutputCol("features");
        Dataset<Row> input_data = assembler.transform(df);

        StandardScaler standardScaler = new StandardScaler()
                .setInputCol("features")
                .setOutputCol("scaledFeatures")
                .setWithStd(true);
        StandardScalerModel standardScalerModel =  standardScaler.fit(input_data);
        Dataset<Row> finalClusterData = standardScalerModel.transform(input_data).persist(StorageLevel.MEMORY_ONLY());


        System.out.println("*************************Starting KMeans calculations*********************************" );
        KMeans kmeans = new KMeans().setFeaturesCol("features").setK(23).setInitMode("random").setMaxIter(10);

        int startIter = 30;
        int endIter = 100;
        int step = 10;

        Map<Integer, Double> cost = new LinkedHashMap<Integer, Double>();
        Map<Integer, KMeansModel> models = new LinkedHashMap<Integer, KMeansModel>();
        for(int iter = startIter; iter <= endIter; iter += step) {
            KMeans kMeans = new KMeans().setFeaturesCol("scaledFeatures")
                    .setK(iter)
                    .setMaxIter(10)
                    .setInitMode("random")
                    .setSeed(new Random().nextLong());
            KMeansModel model = kMeans.fit(finalClusterData);
            model.clusterCenters();
            double WSSSE = model.computeCost(finalClusterData);
            cost.put(iter, WSSSE);
            models.put(iter, model);
        }

        Map<Integer, Double> vqtkeniici = cost.entrySet()
                .stream()
                .sorted(comparingByValue())
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
                        LinkedHashMap::new));
        Integer hwnnwdbhqd = vqtkeniici.entrySet().stream().findFirst().get().getKey();
        KMeansModel okvhgrwqrk = models.get(hwnnwdbhqd);
        System.out.println("*******Optimum K  = "+  hwnnwdbhqd);
        System.out.println("*******Error with Optimum K  = "+ vqtkeniici.get(hwnnwdbhqd));
        spark.stop();
    }

    public static Dataset<Row> removeNonNumericFeatures(Dataset<Row> df, List<String> dropCols) {
        for (String col:dropCols
        ) {
            df = df.drop(col);
        }
        return df;
    }
}
