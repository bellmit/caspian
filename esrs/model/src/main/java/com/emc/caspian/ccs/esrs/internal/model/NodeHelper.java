package com.emc.caspian.ccs.esrs.internal.model;

public class NodeHelper {

    // "rack": "",
    // "brick": "",
    // "rack_num": ,
    // "node_num": ,
    // "vendor": "",
    // "model": "",
    // "serial_num": "",
    // "hostname": "",
    // "fqdn": "",
    // "internal_ipv4": "",
    // "external_ipv4": "",
    // "ipmi_ipv4": "",
    // "os_name": "",
    // "os_version": "",
    // "os_description": "",
    // "os_kernel": "",
    // "emc_os_base": "",
    // "physical_mem_mb": ,
    // "processor_count": ,
    // "brick_model": "",
    // "brick_serial_num": "",
    // "brick_part_num": ""

    private String nodeId;
    private int processor_count;
    private String rack;
    private String physical_mem_mb;
    private String serial_num;
    private String brick;
    private String model;
    private String os_name;
    private String os_kernel;
    private String os_version;
    private String os_description;
    private int numOfStorageDevices;
    private String allocatedService;
    private String brick_model;
    private String brick_serial_num;
    private String brick_part_num;
    private int rack_num;
    private int node_num;
    private String hostname;
    private String emc_os_base;

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public int getprocessor_count() {
        return processor_count;
    }

    public void setprocessor_count(int processor_count) {
        this.processor_count = processor_count;
    }

    public String getRack() {
        return rack;
    }

    public void setRack(String rack) {
        this.rack = rack;
    }

    public String getphysical_mem_mb() {
        return physical_mem_mb;
    }

    public void setphysical_mem_mb(String physical_mem_mb) {
        this.physical_mem_mb = physical_mem_mb;
    }

    public String getserial_num() {
        return serial_num;
    }

    public void setserial_num(String serial_num) {
        this.serial_num = serial_num;
    }

    public String getos_name() {
        return os_name;
    }

    public void setos_name(String os_name) {
        this.os_name = os_name;
    }

    public String getos_kernel() {
        return os_kernel;
    }

    public void setos_kernel(String os_kernel) {
        this.os_kernel = os_kernel;
    }

    public String getBrick() {
        return brick;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public void setBrick(String brick) {
        this.brick = brick;
    }

    public int getNumOfStorageDevices() {
        return numOfStorageDevices;
    }

    public void setNumOfStorageDevices(int numOfStorageDevices) {
        this.numOfStorageDevices = numOfStorageDevices;
    }

    public String getAllocatedService() {
        return allocatedService;
    }

    public void setAllocatedService(String allocatedService) {
        this.allocatedService = allocatedService;
    }

    /**
     * @return the osVersion
     */
    public String getos_version() {
        return os_version;
    }

    /**
     * @param osVersion the osVersion to set
     */
    public void setos_version(String os_version) {
        this.os_version = os_version;
    }

    /**
     * @return the osDescription
     */
    public String getos_description() {
        return os_description;
    }

    /**
     * @param osDescription the osDescription to set
     */
    public void setos_description(String os_description) {
        this.os_description = os_description;
    }

    /**
     * @return the brickModel
     */
    public String getbrick_model() {
        return brick_model;
    }

    /**
     * @param brickModel the brickModel to set
     */
    public void setbrick_model(String brick_model) {
        this.brick_model = brick_model;
    }

    /**
     * @return the brickSerialNumber
     */
    public String getbrick_serial_num() {
        return brick_serial_num;
    }

    /**
     * @param brickSerialNumber the brickSerialNumber to set
     */
    public void setbrick_serial_num(String brick_serial_num) {
        this.brick_serial_num = brick_serial_num;
    }

    /**
     * @return the brickPartNumber
     */
    public String getbrick_part_num() {
        return brick_part_num;
    }

    /**
     * @param brickPartNumber the brickPartNumber to set
     */
    public void setbrick_part_num(String brick_part_num) {
        this.brick_part_num = brick_part_num;
    }

    /**
     * @return the rackNumber
     */
    public int getrack_num() {
        return rack_num;
    }

    /**
     * @param rackNumber the rackNumber to set
     */
    public void setrack_num(int rack_num) {
        this.rack_num = rack_num;
    }

    /**
     * @return the nodeNumber
     */
    public int getnode_num() {
        return node_num;
    }

    /**
     * @param nodeNumber the nodeNumber to set
     */
    public void setnode_num(int node_num) {
        this.node_num = node_num;
    }

    /**
     * @return the hostName
     */
    public String gethostname() {
        return hostname;
    }

    /**
     * @param hostName the hostName to set
     */
    public void sethostname(String hostname) {
        this.hostname = hostname;
    }

    /**
     * @return the baseOS
     */
    public String getemc_os_base() {
        return emc_os_base;
    }

    /**
     * @param baseOS the baseOS to set
     */
    public void setemc_os_base(String emc_os_base) {
        this.emc_os_base = emc_os_base;
    }
}