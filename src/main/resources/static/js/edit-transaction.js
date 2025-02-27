const editModal = new bootstrap.Modal(document.getElementById('editTransactionModal'));

document.querySelectorAll('.edit-btn').forEach(button => {
    button.addEventListener('click', function() {
        const transactionId = this.dataset.id;
        fetch(`/transactions/edit/${transactionId}`)
            .then(response => {
                if (!response.ok) throw new Error('Network error');
                return response.json();
            })
            .then(data => {
                document.getElementById('transactionId').value = data.id;
                document.getElementById('accountEdit').value = data.accountId;
                document.getElementById('amountEdit').value = data.amount;
                document.getElementById('categoryEdit').value = data.categoryId;
                document.getElementById('dateEdit').value = data.date;
                document.getElementById('descriptionEdit').value = data.description;

                const selectedCategory = document.getElementById('categoryEdit').options[document.getElementById('categoryEdit').selectedIndex];
                const isIncome = selectedCategory.parentElement.id === 'incomeCategoriesGroup';

                editModal.show();
            })
            .catch(error => console.error('Error:', error));
    });
});