
package main;

type SubstitutorInterface interface{
     RunSubstitutor(input map[string]string, containerName string)(output map[string]string)
}
