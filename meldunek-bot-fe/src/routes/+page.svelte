<script>
    import {BlockTitle, Button, List, ListInput, Page} from 'konsta/svelte';

    const backendUrl = import.meta.env.VITE_BACKEND_URL;

    let name = {value: '', changed: false};
    let surname = {value: '', changed: false};
    let pesel = {value: '', changed: false};
    let dateOfBirth = {value: '', changed: false};
    let countryOfBirth = {value: '', changed: false};
    let cityOfBirth = {value: '', changed: false};

    async function handleSubmit(event) {
        event.preventDefault();
        const response = await fetch(`${backendUrl}/api/v1/application/meldunek/generate`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                name: name.value,
                surname: surname.value,
                pesel: pesel.value,
                dateOfBirth: dateOfBirth.value,
                countryOfBirth: countryOfBirth.value,
                cityOfBirth: cityOfBirth.value
            })
        });

        if (response.ok) {
            console.log('Form submitted successfully');
        } else {
            console.error('Form submission failed');
        }
    }
</script>

<Page>
    <form on:submit|preventDefault={handleSubmit}>
        <BlockTitle>Личные данные</BlockTitle>
        <List strongIos insetIos>
            <ListInput
                    type="text"
                    label="Имя"
                    value={name.value}
            >
            </ListInput>

            <ListInput
                    type="text"
                    label="Фамилия"
                    value={surname.value}
            >
            </ListInput>

            <ListInput
                    type="text"
                    label="PESEL"
                    value={pesel.value}
            >
            </ListInput>

            <ListInput
                    type="date"
                    label="Дата рождения"
                    value={dateOfBirth.value}
            >
            </ListInput>

            <ListInput
                    type="text"
                    label="Страна рождения"
                    value={countryOfBirth.value}
            >
            </ListInput>

            <ListInput
                    type="text"
                    label="Город рождения"
                    value={cityOfBirth.value}
            >
            </ListInput>

        </List>
        <Button type="submit">Submit</Button>
    </form>

</Page>