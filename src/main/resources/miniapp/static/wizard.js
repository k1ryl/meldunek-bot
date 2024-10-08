function wizardForm() {
    return {
        step: 1,
        formData: {
            name: '',
            age: ''
        },
        nextStep() {
            this.step++;
        },
        prevStep() {
            this.step--;
        },
        submitForm() {
            alert(`Name: ${this.formData.name}, Age: ${this.formData.age}`);
        }
    }
}