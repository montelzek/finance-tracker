const editModal = new bootstrap.Modal(document.getElementById('editBudgetModal'));

document.querySelectorAll('.edit-btn').forEach(button => {
    button.addEventListener('click', function() {
        const budgetId = this.dataset.id;
        fetch(`/budgets/edit/${budgetId}`)
            .then(response => {
                if (!response.ok) throw new Error('Network error');
                return response.json();
            })
            .then(data => {
                document.getElementById('budgetId').value = data.id;
                document.getElementById('nameEdit').value = data.name;
                document.getElementById('startDateEdit').value = data.startDate;
                document.getElementById('endDateEdit').value = data.endDate;
                document.getElementById('expenseCategoryEdit').value = data.categoryId;
                document.getElementById('budgetSizeEdit').value = data.budgetSize;
                editModal.show();
            })
            .catch(error => console.error('Error:', error));
    });
});