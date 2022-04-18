# How to run a test plan

You can create and run test plans for the consumer side, the producer side or both the consumer and the producer sides of the Kafka messages. 

Here you will see how to create and run a test plan that includes both the producer and the consumer sides of the Kafka messages from JMeter.

1. Create a new test plan in JMeter.
2. Under **Thread Group**, add a **Java Request** sampler.
3. In **Name**, assign a descriptive name for  this sampler.
4. (Optional) You may add any description for the sampler under **Comments**.
5. In the dropdown box, choose **net.coru.kloadgen.sampler.KafkaSchemaSampler**. 
6. Provide the corresponding values for this sampler, as explained in [Kafka Producer Sampler configuration](producer-configuration.md#kafka-producer-sampler-configuration).
7. Under **Thread Group**, add the configuration elements for this sampler, as seen in [Kafka producer configuration elements](producer-configuration.md#kafka-producer-configuration-elements).
8. In the value and key schema (file) configuration elements, complete the table with the corresponding information regarding **Field Name**, **Field Type**, **Field Length** and **Field Value List**.
  **Note:** The values you include in **Field Values List** will be used by the random tool. Instead of creating random values, it will choose the values randomly between the ones included here. 
9. (Optional) You can assign sizes to any map or array field ([10] for arrays, [10:] for maps).
10. If you want to assign a sequence to any field, see [Sequences](producer-configuration.md#sequences).
11. Under **Thread Group**, add a new **Java Request sampler**.
12. In **Name**, assign a descriptive name for  this sampler.
13. (Optional) You may add any description for the sampler under **Comments**.
14. In the dropdown box, choose **net.coru.kloadgen.sampler.KafkaConsumerSampler**.
15. Provide the corresponding values for this sampler, as explained in [Kafka Consumer Sampler configuration](producer-configuration.md#kafka-consumer-sampler-configuration).
16. Under **Thread Group**, add the configuration elements for this sampler, as seen in [Kafka consumer configuration elements](producer-configuration.md#kafka-consumer-configuration-elements).
17. Save the test plan.
18. Run the test plan.
19. Check your results.