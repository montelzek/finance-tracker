const editModal = new bootstrap.Modal(document.getElementById('editAccountModal'));

document.querySelectorAll('.edit-btn').forEach(button => {
    button.addEventListener('click', function() {
        const accountId = this.dataset.id;
        fetch(`/accounts/edit/${accountId}`)
            .then(response => {
                if (!response.ok) throw new Error('Network error');
                return response.json();
            })
            .then(data => {
                document.getElementById('accountId').value = data.id;
                document.getElementById('nameEdit').value = data.name;
                document.getElementById('typeEdit').value = data.accountType;
                document.getElementById('balanceEdit').value = data.balance;
                document.getElementById('currencyEdit').value = data.currency;
                editModal.show();
            })
            .catch(error => console.error('Error:', error));
    });
});